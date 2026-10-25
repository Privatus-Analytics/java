package com.privatusanalytics.spring;

import com.privatusanalytics.PrivatusSettings;
import com.privatusanalytics.spring.thymeleaf.PrivatusDialect;
import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sets up Privatus Analytics in servlet web apps: the settings, the renderer, the Thymeleaf
 * dialect, the {@code privatusAnalytics} model attribute and the optional auto inject filter.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
@EnableConfigurationProperties(PrivatusAnalyticsProperties.class)
public class PrivatusAnalyticsAutoConfiguration {
  /** The environment variable used when {@code privatus.analytics.site-id} is not set. */
  public static final String SITE_ID_VARIABLE = PrivatusSettings.SITE_ID_VARIABLE;

  /** Creates the auto-configuration. */
  public PrivatusAnalyticsAutoConfiguration() {}

  /**
   * The base settings from {@code privatus.analytics} and the active profiles.
   *
   * @param properties the properties
   * @param environment the Spring environment
   * @return the settings
   */
  static PrivatusSettings settings(
      PrivatusAnalyticsProperties properties, Environment environment) {
    Boolean enabled = properties.getEnabled();
    if (enabled == null) {
      List<String> profiles = properties.getEnabledProfiles();
      enabled =
          profiles != null
              && !profiles.isEmpty()
              && environment.acceptsProfiles(Profiles.of(profiles.toArray(String[]::new)));
    }
    return PrivatusSettings.builder()
        .variables(environment::getProperty)
        .siteId(properties.getSiteId())
        .scriptHost(properties.getScriptHost())
        .modules(properties.getModules())
        .excludedPaths(properties.getExcludedPaths())
        .domains(properties.getDomains())
        .spa(properties.getSpa())
        .manual(properties.isManual())
        .enabled(enabled)
        .environments(Arrays.asList(environment.getActiveProfiles()))
        .extraAttributes(properties.getExtraAttributes())
        .props(properties.getProps())
        .queueStub(properties.isQueueStub())
        .build();
  }

  /**
   * The renderer used by every integration.
   *
   * @param properties the properties
   * @param environment the Spring environment
   * @param propsProviders your page props providers
   * @return the renderer
   */
  @Bean
  @ConditionalOnMissingBean
  public PrivatusAnalyticsRenderer privatusAnalyticsRenderer(
      PrivatusAnalyticsProperties properties,
      Environment environment,
      ObjectProvider<PrivatusPagePropsProvider> propsProviders) {
    return new PrivatusAnalyticsRenderer(
        settings(properties, environment),
        properties.getNonceAttribute(),
        propsProviders.orderedStream().toList());
  }

  /**
   * The filter that adds the tag to HTML responses. Only when {@code
   * privatus.analytics.auto-inject=true}.
   *
   * @param renderer the renderer
   * @return the filter registration
   */
  @Bean
  @ConditionalOnProperty(prefix = "privatus.analytics", name = "auto-inject", havingValue = "true")
  @ConditionalOnMissingBean(name = "privatusAnalyticsFilter")
  public FilterRegistrationBean<PrivatusAnalyticsFilter> privatusAnalyticsFilter(
      PrivatusAnalyticsRenderer renderer) {
    FilterRegistrationBean<PrivatusAnalyticsFilter> registration =
        new FilterRegistrationBean<>(new PrivatusAnalyticsFilter(renderer));
    registration.setName("privatusAnalyticsFilter");
    registration.addUrlPatterns("/*");
    registration.setDispatcherTypes(DispatcherType.REQUEST);
    registration.setOrder(Ordered.LOWEST_PRECEDENCE - 100);
    return registration;
  }

  /** Spring MVC: the {@code privatusAnalytics} request and model attribute. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(WebMvcConfigurer.class)
  @ConditionalOnProperty(
      prefix = "privatus.analytics",
      name = "expose-model-attribute",
      havingValue = "true",
      matchIfMissing = true)
  static class MvcConfiguration {
    @Bean
    WebMvcConfigurer privatusAnalyticsWebMvcConfigurer(PrivatusAnalyticsRenderer renderer) {
      PrivatusAnalyticsInterceptor interceptor = new PrivatusAnalyticsInterceptor(renderer);
      return new WebMvcConfigurer() {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
          registry.addInterceptor(interceptor);
        }
      };
    }
  }

  /** Thymeleaf: the {@code privatus:} dialect. */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(name = "org.thymeleaf.spring6.SpringTemplateEngine")
  static class ThymeleafConfiguration {
    @Bean
    @ConditionalOnMissingBean
    PrivatusDialect privatusDialect(PrivatusAnalyticsRenderer renderer) {
      return new PrivatusDialect(renderer);
    }
  }
}
