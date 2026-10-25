package com.privatusanalytics.spring;

import static org.assertj.core.api.Assertions.assertThat;

import com.privatusanalytics.spring.thymeleaf.PrivatusDialect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;

class AutoConfigurationTest {
  final WebApplicationContextRunner runner =
      new WebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(PrivatusAnalyticsAutoConfiguration.class))
          .withPropertyValues("privatus.analytics.site-id=pa_TEST123456");

  static String tag(org.springframework.context.ApplicationContext context) {
    return context.getBean(PrivatusAnalyticsRenderer.class).tag(new MockHttpServletRequest());
  }

  @Test
  void offWithoutTheProdProfile() {
    runner.run(context -> assertThat(tag(context)).isEmpty());
    runner
        .withPropertyValues("spring.profiles.active=dev")
        .run(context -> assertThat(tag(context)).isEmpty());
  }

  @Test
  void onWithTheProdOrProductionProfile() {
    runner
        .withPropertyValues("spring.profiles.active=prod")
        .run(context -> assertThat(tag(context)).contains("data-site=\"pa_TEST123456\""));
    runner
        .withPropertyValues("spring.profiles.active=cloud,production")
        .run(context -> assertThat(tag(context)).contains("data-site=\"pa_TEST123456\""));
  }

  @Test
  void enabledProfilesCanChange() {
    runner
        .withPropertyValues(
            "spring.profiles.active=staging", "privatus.analytics.enabled-profiles=staging")
        .run(context -> assertThat(tag(context)).contains("pa.js"));
  }

  @Test
  void enabledForcesTheTagOnOrOff() {
    runner
        .withPropertyValues("privatus.analytics.enabled=true")
        .run(context -> assertThat(tag(context)).contains("pa.js"));
    runner
        .withPropertyValues("spring.profiles.active=prod", "privatus.analytics.enabled=false")
        .run(context -> assertThat(tag(context)).isEmpty());
  }

  @Test
  void theSiteIdFallsBackToPrivatusSiteId() {
    new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PrivatusAnalyticsAutoConfiguration.class))
        .withPropertyValues("PRIVATUS_SITE_ID=pa_FROMENV123", "privatus.analytics.enabled=true")
        .run(context -> assertThat(tag(context)).contains("data-site=\"pa_FROMENV123\""));
  }

  @Test
  void anExplicitSiteIdWins() {
    runner
        .withPropertyValues("PRIVATUS_SITE_ID=pa_FROMENV123", "privatus.analytics.enabled=true")
        .run(context -> assertThat(tag(context)).contains("data-site=\"pa_TEST123456\""));
  }

  @Test
  void printsNothingWithoutASiteId() {
    new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PrivatusAnalyticsAutoConfiguration.class))
        .withPropertyValues("privatus.analytics.enabled=true")
        .run(context -> assertThat(tag(context)).isEmpty());
  }

  @Test
  void everySettingReachesTheTag() {
    runner
        .withPropertyValues(
            "privatus.analytics.enabled=true",
            "privatus.analytics.script-host=https://stats.example.com/",
            "privatus.analytics.modules=engage,auto",
            "privatus.analytics.excluded-paths=/admin/*,/preview/*",
            "privatus.analytics.domains=example.com,www.example.com",
            "privatus.analytics.spa=hash",
            "privatus.analytics.manual=true",
            "privatus.analytics.extra-attributes.data-debug=true",
            "privatus.analytics.props.plan=pro",
            "privatus.analytics.queue-stub=true")
        .run(
            context -> {
              String tag = tag(context);
              assertThat(tag)
                  .startsWith("<script>window.privatus=window.privatus||{q:[]};")
                  .contains("{\"plan\":\"pro\"}")
                  .contains("src=\"https://stats.example.com/js/pa.js\"")
                  .contains("data-modules=\"engage,auto\"")
                  .contains("data-exclude=\"/admin/*,/preview/*\"")
                  .contains("data-domains=\"example.com,www.example.com\"")
                  .contains("data-spa=\"hash\"")
                  .contains("data-manual=\"true\"")
                  .contains("data-props=\"privatusPageProps\"")
                  .contains("data-debug=\"true\"");
            });
  }

  @Test
  void anEmptyModulesListLoadsNone() {
    runner
        .withPropertyValues("privatus.analytics.enabled=true", "privatus.analytics.modules=")
        .run(context -> assertThat(tag(context)).contains("data-modules=\"\""));
  }

  @Test
  void registersTheDialectAndLeavesTheFilterOff() {
    runner.run(
        context -> {
          assertThat(context).hasSingleBean(PrivatusDialect.class);
          assertThat(context).doesNotHaveBean("privatusAnalyticsFilter");
        });
  }

  @Test
  void registersTheFilterWhenAutoInjectIsOn() {
    runner
        .withPropertyValues("privatus.analytics.auto-inject=true")
        .run(context -> assertThat(context).hasBean("privatusAnalyticsFilter"));
  }

  @Test
  void staysOutOfNonWebApps() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(PrivatusAnalyticsAutoConfiguration.class))
        .run(context -> assertThat(context).doesNotHaveBean(PrivatusAnalyticsRenderer.class));
  }

  @Test
  void theHelperForJspAndFreeMarker() {
    runner
        .withPropertyValues("privatus.analytics.enabled=true")
        .run(
            context -> {
              PrivatusAnalyticsHelper helper =
                  new PrivatusAnalyticsHelper(
                      context.getBean(PrivatusAnalyticsRenderer.class),
                      new MockHttpServletRequest());
              assertThat(helper.getTag()).contains("pa.js");
              assertThat(helper.isEnabled()).isTrue();
              assertThat(helper.eventAttrs("Export CSV", java.util.Map.of("format", "csv")))
                  .isEqualTo(
                      "data-privatus-event=\"Export CSV\" data-privatus-prop-format=\"csv\"");
              assertThat(helper.clickAttr("pricing.cta"))
                  .isEqualTo("data-privatus-click=\"pricing.cta\"");
              assertThat(helper.tagWith(java.util.Map.of("modules", "vitals")))
                  .contains("data-modules=\"vitals\"");
            });
  }
}
