package com.privatusanalytics;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Builds the HTML attributes that make the tracker send a custom event when someone clicks an
 * element (or submits a form).
 *
 * <pre>{@code
 * EventAttributes.render("Export CSV", Map.of("format", "csv"))
 * // data-privatus-event="Export CSV" data-privatus-prop-format="csv"
 * }</pre>
 *
 * <p>The attributes are plain markup, so they print even when the tag is switched off. That way
 * your development and production HTML match.
 */
public final class EventAttributes {
  /** The attribute that names the event. */
  public static final String EVENT_ATTRIBUTE = "data-privatus-event";

  /** The start of each property attribute name. */
  public static final String PROP_PREFIX = "data-privatus-prop-";

  /** Longest event name, in characters. Longer names are cut. */
  public static final int MAX_NAME_LENGTH = 120;

  /** Longest property key, in characters. */
  public static final int MAX_KEY_LENGTH = Values.MAX_KEY_LENGTH;

  /** Most properties kept for one event. */
  public static final int MAX_PROPS = Values.MAX_PROPS;

  /** Longest property value, in characters. */
  public static final int MAX_VALUE_LENGTH = Values.MAX_VALUE_LENGTH;

  private EventAttributes() {}

  /**
   * The attributes as an ordered map of name to raw (not yet escaped) value.
   *
   * @param name the event name, for example {@code "Export CSV"}
   * @param props event properties, may be null
   * @return the attributes, or an empty map when the name is empty
   */
  public static Map<String, String> attributes(String name, Map<?, ?> props) {
    String eventName = name == null ? "" : Values.truncate(name.strip(), MAX_NAME_LENGTH);
    if (eventName.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, String> attributes = new LinkedHashMap<>();
    attributes.put(EVENT_ATTRIBUTE, eventName);
    for (Map.Entry<String, String> prop : props(props).entrySet()) {
      attributes.put(PROP_PREFIX + prop.getKey(), prop.getValue());
    }
    return attributes;
  }

  /**
   * The event properties after the key and value rules: keys lowercased with other characters
   * turned into underscores, null values dropped, values turned into text, at most 30 kept.
   *
   * @param props event properties, may be null
   * @return the clean properties in their original order
   */
  public static Map<String, String> props(Map<?, ?> props) {
    Map<String, String> clean = new LinkedHashMap<>();
    if (props == null) {
      return clean;
    }
    for (Map.Entry<?, ?> entry : props.entrySet()) {
      if (clean.size() >= MAX_PROPS) {
        break;
      }
      String key = Values.normalizeKey(entry.getKey());
      Object value = entry.getValue();
      if (key.isEmpty() || value == null || clean.containsKey(key)) {
        continue;
      }
      clean.put(key, Values.truncate(valueText(value), MAX_VALUE_LENGTH));
    }
    return clean;
  }

  /**
   * The attributes as escaped HTML, ready to put inside an opening tag.
   *
   * @param name the event name
   * @param props event properties, may be null
   * @return the attribute text, or an empty string when the name is empty
   */
  public static String render(String name, Map<?, ?> props) {
    StringJoiner out = new StringJoiner(" ");
    attributes(name, props)
        .forEach((attribute, value) -> out.add(Html.attribute(attribute, value)));
    return out.toString();
  }

  /**
   * The attributes for an event without properties.
   *
   * @param name the event name
   * @return the attribute text, or an empty string when the name is empty
   */
  public static String render(String name) {
    return render(name, null);
  }

  private static String valueText(Object value) {
    if (Values.isList(value)) {
      StringJoiner joined = new StringJoiner(",");
      for (Object item : Values.items(value)) {
        joined.add(Values.text(item));
      }
      return joined.toString();
    }
    return Values.text(value);
  }
}
