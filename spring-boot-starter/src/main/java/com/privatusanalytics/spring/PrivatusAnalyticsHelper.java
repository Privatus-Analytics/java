package com.privatusanalytics.spring;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * The {@code privatusAnalytics} object in your request and model, for JSP and FreeMarker.
 *
 * <pre>
 * JSP:        ${privatusAnalytics.tag}
 * FreeMarker: ${privatusAnalytics.tag?no_esc}
 * </pre>
 */
public class PrivatusAnalyticsHelper {
  /** The request and model attribute name. */
  public static final String ATTRIBUTE = "privatusAnalytics";

  private final PrivatusAnalyticsRenderer renderer;
  private final HttpServletRequest request;

  /**
   * Creates a helper for one request.
   *
   * @param renderer the renderer
   * @param request the current request
   */
  public PrivatusAnalyticsHelper(PrivatusAnalyticsRenderer renderer, HttpServletRequest request) {
    this.renderer = renderer;
    this.request = request;
  }

  /**
   * The tag for this request.
   *
   * @return the HTML, or an empty string when nothing should print
   */
  public String getTag() {
    return renderer.tag(request);
  }

  /**
   * The tag with per-call overrides. (Not named {@code tag}, so it does not hide the {@code tag}
   * property in FreeMarker.)
   *
   * @param overrides per-call settings, for example {@code {"modules": ["engage", "auto"]}}
   * @return the HTML, or an empty string when nothing should print
   */
  public String tagWith(Map<?, ?> overrides) {
    return renderer.tag(request, overrides);
  }

  /**
   * Event attributes.
   *
   * @param name the event name
   * @param props event properties, may be null
   * @return the attribute text
   */
  public String eventAttrs(String name, Map<?, ?> props) {
    return renderer.eventAttrs(name, props);
  }

  /**
   * Event attributes without properties.
   *
   * @param name the event name
   * @return the attribute text
   */
  public String eventAttrs(String name) {
    return renderer.eventAttrs(name, null);
  }

  /**
   * The click map name attribute.
   *
   * @param name the click map name
   * @return the attribute text
   */
  public String clickAttr(String name) {
    return renderer.clickAttr(name);
  }

  /**
   * Whether the tag prints.
   *
   * @return true when it prints
   */
  public boolean isEnabled() {
    return renderer.isEnabled();
  }
}
