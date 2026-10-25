package com.privatusanalytics.example;

import com.privatusanalytics.spring.PrivatusAnalyticsFilter;
import com.privatusanalytics.spring.PrivatusAnalyticsRenderer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Conformance example app for the Privatus Analytics Spring Boot starter.
 *
 * <p>Built by the conformance suite (https://github.com/Privatus-Analytics/conformance). See its
 * CONTRACT.md for what each route must render. The site id and script host come from
 * PRIVATUS_SITE_ID and PRIVATUS_SCRIPT_HOST.
 */
@SpringBootApplication
@Controller
public class ExampleApplication {
  static final String NONCE = "testnonce";
  static final String NASTY = "</script><script>window.__pwned=1</script>  &<>\"'";
  static final String AUTO_PAGE =
      """
      <!doctype html>
      <html lang="en">
      <head>
      <meta charset="utf-8">
      <title>Auto inject</title>
      </head>
      <body><h1>Auto inject</h1><p>No tag in the template. The filter adds it.</p></body>
      </html>
      """;

  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }

  static Map<String, Object> pageProps() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("plan", "pro");
    props.put("role", "admin");
    props.put("beta", true);
    props.put("seats", 3);
    return props;
  }

  /**
   * Puts the nonce in the cspNonce request attribute, where the starter looks for it, and sends a
   * matching policy. A real app makes a new random nonce for each request.
   */
  @Bean
  FilterRegistrationBean<OncePerRequestFilter> contentSecurityPolicy() {
    String host = System.getenv().getOrDefault("PRIVATUS_SCRIPT_HOST", "https://privatusstats.com");
    String scriptHost = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
    OncePerRequestFilter filter =
        new OncePerRequestFilter() {
          @Override
          protected void doFilterInternal(
              HttpServletRequest request, HttpServletResponse response, FilterChain chain)
              throws ServletException, IOException {
            request.setAttribute("cspNonce", NONCE);
            response.setHeader(
                "Content-Security-Policy", "script-src 'nonce-" + NONCE + "' " + scriptHost);
            chain.doFilter(request, response);
          }
        };
    FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(filter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }

  /** Auto inject on /auto only, so the other routes test the Thymeleaf dialect alone. */
  @Bean
  FilterRegistrationBean<PrivatusAnalyticsFilter> privatusAnalyticsFilter(
      PrivatusAnalyticsRenderer renderer) {
    FilterRegistrationBean<PrivatusAnalyticsFilter> registration =
        new FilterRegistrationBean<>(new PrivatusAnalyticsFilter(renderer));
    registration.addUrlPatterns("/auto");
    return registration;
  }

  @GetMapping("/")
  String home(Model model) {
    model.addAttribute("props", pageProps());
    return "home";
  }

  @GetMapping("/event")
  String event(Model model) {
    model.addAttribute("props", pageProps());
    return "event";
  }

  @GetMapping("/nasty")
  String nasty(Model model) {
    Map<String, Object> props = pageProps();
    props.put("nasty", NASTY);
    model.addAttribute("props", props);
    model.addAttribute("nasty", NASTY);
    return "nasty";
  }

  /** Off with the queue stub on: only the stub prints, so privatus.track still works. */
  @GetMapping("/off")
  String off(Model model) {
    model.addAttribute("props", pageProps());
    model.addAttribute("stub", true);
    return "off";
  }

  /** Off with the queue stub off: nothing prints. */
  @GetMapping("/off-plain")
  String offPlain(Model model) {
    model.addAttribute("props", pageProps());
    model.addAttribute("stub", false);
    return "off";
  }

  @GetMapping(value = "/auto", produces = MediaType.TEXT_HTML_VALUE)
  @ResponseBody
  String auto() {
    return AUTO_PAGE;
  }
}
