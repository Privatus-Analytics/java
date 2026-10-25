package com.privatusanalytics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Builds the tracking script tag.
 *
 * <pre>{@code
 * Tag.render(settings)
 * // <script defer src="https://privatusstats.com/js/pa.js" data-site="pa_YOURSITEID"></script>
 * }</pre>
 *
 * <p>Prints nothing without a site id, or when the settings are not enabled.
 */
public final class Tag {
  /** Text that marks a page that already has the tracker. */
  public static final String MARKER = "/js/pa.js";

  private Tag() {}

  /**
   * Whether a tag would print for these settings.
   *
   * @param settings the settings
   * @return true when there is a site id and the settings are enabled
   */
  public static boolean shouldRender(PrivatusSettings settings) {
    return settings.siteId() != null && settings.isEnabled();
  }

  /**
   * The full HTML: the queue stub (when switched on), the page properties script (when there are
   * properties) and the tracker tag, one per line.
   *
   * @param settings the settings
   * @return the HTML, or an empty string when nothing should print
   */
  public static String render(PrivatusSettings settings) {
    if (!shouldRender(settings)) {
      return "";
    }
    StringJoiner out = new StringJoiner("\n");
    if (settings.queueStub()) {
      out.add(QueueStub.script(settings.nonce()));
    }
    String propsScript = PageProps.script(settings.props(), settings.nonce());
    if (!propsScript.isEmpty()) {
      out.add(propsScript);
    }
    StringJoiner parts = new StringJoiner(" ", "<script ", "></script>");
    attributes(settings).forEach((name, value) -> parts.add(Html.attribute(name, value)));
    out.add(parts.toString());
    return out.toString();
  }

  /**
   * The tracker tag attributes in print order. A null value means a bare attribute such as {@code
   * defer}. Values are not escaped yet.
   *
   * @param settings the settings
   * @return attribute name to raw value
   */
  public static Map<String, String> attributes(PrivatusSettings settings) {
    Map<String, String> attributes = new LinkedHashMap<>();
    attributes.put("defer", null);
    attributes.put("src", settings.scriptHost() + MARKER);
    attributes.put("data-site", settings.siteId());
    // Null keeps the tracker default. An empty modules list loads none.
    if (settings.modules() != null) {
      attributes.put("data-modules", String.join(",", settings.modules()));
    }
    addList(attributes, "data-exclude", settings.excludedPaths());
    addList(attributes, "data-domains", settings.domains());
    String spa = settings.spa();
    if (spa != null && PrivatusSettings.SPA_MODES.contains(spa) && !"auto".equals(spa)) {
      attributes.put("data-spa", spa);
    }
    if (settings.manual()) {
      attributes.put("data-manual", "true");
    }
    if (!PageProps.normalize(settings.props()).isEmpty()) {
      attributes.put("data-props", PageProps.FUNCTION_NAME);
    }
    attributes.putAll(settings.extraAttributes());
    if (settings.nonce() != null) {
      attributes.put("nonce", settings.nonce());
    }
    return attributes;
  }

  private static void addList(Map<String, String> attributes, String name, List<String> values) {
    if (values != null && !values.isEmpty()) {
      attributes.put(name, String.join(",", values));
    }
  }
}
