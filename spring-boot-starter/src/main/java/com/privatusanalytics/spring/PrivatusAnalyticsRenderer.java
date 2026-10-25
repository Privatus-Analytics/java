package com.privatusanalytics.spring;

import com.privatusanalytics.ClickAttribute;
import com.privatusanalytics.EventAttributes;
import com.privatusanalytics.PrivatusSettings;
import com.privatusanalytics.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prints the tag and attributes for a request. The Thymeleaf dialect, the model attribute and the
 * filter all use this bean, and you can inject it yourself.
 */
public class PrivatusAnalyticsRenderer {
  private final PrivatusSettings settings;
  private final String nonceAttribute;
  private final List<PrivatusPagePropsProvider> propsProviders;

  /**
   * Creates a renderer.
   *
   * @param settings the base settings
   * @param nonceAttribute request attribute that holds the nonce, may be null
   * @param propsProviders page props providers, may be empty
   */
  public PrivatusAnalyticsRenderer(
      PrivatusSettings settings,
      String nonceAttribute,
      List<PrivatusPagePropsProvider> propsProviders) {
    this.settings = settings;
    this.nonceAttribute = nonceAttribute;
    this.propsProviders = List.copyOf(propsProviders);
  }

  /**
   * The base settings from {@code privatus.analytics}.
   *
   * @return the settings
   */
  public PrivatusSettings settings() {
    return settings;
  }

  /**
   * Whether the tag prints at all (there is a site id and the tag is switched on).
   *
   * @return true when it prints
   */
  public boolean isEnabled() {
    return Tag.shouldRender(settings);
  }

  /**
   * The settings for one request: the nonce from the request, page properties from your providers,
   * then the overrides.
   *
   * @param request the current request, may be null
   * @param overrides per-call settings, may be null (see {@link
   *     PrivatusSettings.Builder#override(Map)})
   * @return the settings
   */
  public PrivatusSettings settings(HttpServletRequest request, Map<?, ?> overrides) {
    return settings(request, overrides, true);
  }

  private PrivatusSettings settings(
      HttpServletRequest request, Map<?, ?> overrides, boolean withProviders) {
    PrivatusSettings.Builder builder = settings.toBuilder();
    if (request != null) {
      if (nonceAttribute != null && !nonceAttribute.isBlank()) {
        Object nonce = request.getAttribute(nonceAttribute);
        if (nonce != null) {
          builder.nonce(nonce.toString());
        }
      }
      for (PrivatusPagePropsProvider provider :
          withProviders ? propsProviders : List.<PrivatusPagePropsProvider>of()) {
        Map<String, ?> props = provider.pageProps(request);
        if (props != null) {
          builder.addProps(new LinkedHashMap<>(props));
        }
      }
    }
    return builder.override(overrides).build();
  }

  /**
   * The tag for this request.
   *
   * @param request the current request, may be null
   * @return the HTML, or an empty string when nothing should print
   */
  public String tag(HttpServletRequest request) {
    return tag(request, null);
  }

  /**
   * The tag for this request with per-call overrides.
   *
   * <p>When the tag is off (no site id, or not enabled) it prints only the queue stub when that is
   * switched on, and otherwise nothing. Your page props providers are not called then.
   *
   * @param request the current request, may be null
   * @param overrides per-call settings, may be null
   * @return the HTML, or an empty string when nothing should print
   */
  public String tag(HttpServletRequest request, Map<?, ?> overrides) {
    PrivatusSettings withoutProviders = settings(request, overrides, false);
    if (!Tag.shouldRender(withoutProviders)) {
      return Tag.render(withoutProviders);
    }
    return Tag.render(settings(request, overrides));
  }

  /**
   * Event attributes. They print even when the tag is off.
   *
   * @param name the event name
   * @param props event properties, may be null
   * @return the attribute text
   */
  public String eventAttrs(String name, Map<?, ?> props) {
    return EventAttributes.render(name, props);
  }

  /**
   * The click map name attribute.
   *
   * @param name the click map name
   * @return the attribute text, or an empty string when the name is not allowed
   */
  public String clickAttr(String name) {
    return ClickAttribute.render(name);
  }
}
