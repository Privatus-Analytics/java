package com.privatusanalytics.spring;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Gives page properties for one request, for example the plan or role of the signed-in account.
 * Declare one as a bean:
 *
 * <pre>{@code
 * @Bean
 * PrivatusPagePropsProvider pageProps() {
 *   return request -> Map.of("plan", currentPlan(request));
 * }
 * }</pre>
 *
 * <p>Never return names, emails, user ids or anything else that identifies a person. Return groups,
 * not people.
 */
@FunctionalInterface
public interface PrivatusPagePropsProvider {
  /**
   * The properties for this request.
   *
   * @param request the current request
   * @return property name to value, may be null or empty
   */
  Map<String, ?> pageProps(HttpServletRequest request);
}
