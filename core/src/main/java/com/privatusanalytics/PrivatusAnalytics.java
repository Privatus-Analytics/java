package com.privatusanalytics;

import java.util.Map;

/**
 * One place to reach every helper. The library only prints HTML. It never sends events from the
 * server, sets no cookies and reads nothing about your visitors.
 *
 * <pre>{@code
 * PrivatusAnalytics.tag(settings);
 * PrivatusAnalytics.eventAttrs("Export CSV", Map.of("format", "csv"));
 * PrivatusAnalytics.clickAttr("pricing.cta");
 * }</pre>
 */
public final class PrivatusAnalytics {
  /** The library version. */
  public static final String VERSION = "1.0.1";

  private PrivatusAnalytics() {}

  /**
   * The tracking script tag. See {@link Tag#render(PrivatusSettings)}.
   *
   * @param settings the settings
   * @return the HTML, or an empty string when nothing should print
   */
  public static String tag(PrivatusSettings settings) {
    return Tag.render(settings);
  }

  /**
   * Event attributes for a button, link or form. See {@link EventAttributes#render(String, Map)}.
   *
   * @param name the event name, for example {@code "Export CSV"}
   * @param props event properties, may be null
   * @return the attribute text
   */
  public static String eventAttrs(String name, Map<?, ?> props) {
    return EventAttributes.render(name, props);
  }

  /**
   * Event attributes without properties.
   *
   * @param name the event name
   * @return the attribute text
   */
  public static String eventAttrs(String name) {
    return EventAttributes.render(name);
  }

  /**
   * The click map name attribute. See {@link ClickAttribute#render(String)}.
   *
   * @param name the click map name, for example {@code "pricing.cta"}
   * @return the attribute text, or an empty string when the name is not allowed
   */
  public static String clickAttr(String name) {
    return ClickAttribute.render(name);
  }

  /**
   * Page properties as script-safe JSON. See {@link PageProps#toJson(Map)}.
   *
   * @param props the properties
   * @return the JSON text
   */
  public static String pagePropsJson(Map<?, ?> props) {
    return PageProps.toJson(props);
  }

  /**
   * The queue stub script. See {@link QueueStub#script(String)}.
   *
   * @param nonce a Content Security Policy nonce, may be null
   * @return the script element
   */
  public static String queueStub(String nonce) {
    return QueueStub.script(nonce);
  }
}
