package com.privatusanalytics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Page properties: groups such as plan or role that the tracker sends with every pageview and event
 * on the page.
 *
 * <p>Never put names, emails, user ids or anything else that points to one person in here. Privatus
 * counts visitors without any id. Put groups, not people.
 */
public final class PageProps {
  /** The name of the browser function the tracker calls to read the properties. */
  public static final String FUNCTION_NAME = "privatusPageProps";

  /** Most properties kept. */
  public static final int MAX_PROPS = Values.MAX_PROPS;

  /** Most items kept in one list value. */
  public static final int MAX_LIST_ITEMS = Values.MAX_LIST_ITEMS;

  private PageProps() {}

  /**
   * The properties after the key and value rules. Strings are cut to 500 characters, numbers and
   * booleans stay as they are, lists stay lists (at most 30 items), nulls are dropped. Anything
   * else becomes text.
   *
   * @param props the properties, may be null
   * @return the clean properties in their original order
   */
  public static Map<String, Object> normalize(Map<?, ?> props) {
    Map<String, Object> clean = new LinkedHashMap<>();
    if (props == null) {
      return clean;
    }
    for (Map.Entry<?, ?> entry : props.entrySet()) {
      if (clean.size() >= MAX_PROPS) {
        break;
      }
      String key = Values.normalizeKey(entry.getKey());
      if (key.isEmpty() || clean.containsKey(key)) {
        continue;
      }
      Object value = normalizeValue(entry.getValue(), true);
      if (value != null) {
        clean.put(key, value);
      }
    }
    return clean;
  }

  /**
   * The properties as a JSON object that is safe inside a {@code <script>} element.
   *
   * @param props the properties, may be null
   * @return JSON text, {@code {}} when there are none
   */
  public static String toJson(Map<?, ?> props) {
    StringJoiner out = new StringJoiner(",", "{", "}");
    normalize(props).forEach((key, value) -> out.add(jsonString(key) + ":" + jsonValue(value)));
    return out.toString();
  }

  /**
   * The inline script that defines {@code window.privatusPageProps}.
   *
   * @param props the properties, may be null
   * @param nonce a Content Security Policy nonce, may be null
   * @return the script element, or an empty string when there are no properties
   */
  public static String script(Map<?, ?> props, String nonce) {
    if (normalize(props).isEmpty()) {
      return "";
    }
    return openScript(nonce)
        + "window."
        + FUNCTION_NAME
        + "=function(){return "
        + toJson(props)
        + "}</script>";
  }

  static String openScript(String nonce) {
    return nonce == null || nonce.isEmpty()
        ? "<script>"
        : "<script " + Html.attribute("nonce", nonce) + ">";
  }

  private static Object normalizeValue(Object value, boolean allowList) {
    if (value == null) {
      return null;
    }
    if (value instanceof Boolean) {
      return value;
    }
    if (value instanceof Number number) {
      String text = Values.numberText(number);
      return text == null ? null : new JsonNumber(text);
    }
    if (Values.isList(value)) {
      if (!allowList) {
        return Values.truncate(String.valueOf(value), Values.MAX_VALUE_LENGTH);
      }
      List<Object> items = new ArrayList<>();
      for (Object item : Values.items(value)) {
        if (items.size() >= MAX_LIST_ITEMS) {
          break;
        }
        Object clean = normalizeValue(item, false);
        if (clean != null) {
          items.add(clean);
        }
      }
      return List.copyOf(items);
    }
    return Values.truncate(String.valueOf(value), Values.MAX_VALUE_LENGTH);
  }

  private static String jsonValue(Object value) {
    if (value instanceof Boolean bool) {
      return bool ? "true" : "false";
    }
    if (value instanceof JsonNumber number) {
      return number.text();
    }
    if (value instanceof List<?> list) {
      StringJoiner out = new StringJoiner(",", "[", "]");
      for (Object item : list) {
        out.add(jsonValue(item));
      }
      return out.toString();
    }
    return jsonString(String.valueOf(value));
  }

  /**
   * Writes a JSON string. Besides the usual escapes it turns {@code <}, {@code >}, {@code &},
   * U+2028 and U+2029 into {@code \\u} escapes, so the text cannot close the script element.
   */
  static String jsonString(String value) {
    StringBuilder out = new StringBuilder(value.length() + 2);
    out.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"' -> out.append("\\\"");
        case '\\' -> out.append("\\\\");
        case '\n' -> out.append("\\n");
        case '\r' -> out.append("\\r");
        case '\t' -> out.append("\\t");
        case '\b' -> out.append("\\b");
        case '\f' -> out.append("\\f");
        case '<', '>', '&', ' ', ' ' -> unicode(out, c);
        default -> {
          if (c < 0x20) {
            unicode(out, c);
          } else {
            out.append(c);
          }
        }
      }
    }
    return out.append('"').toString();
  }

  private static void unicode(StringBuilder out, char c) {
    out.append(String.format("\\u%04x", (int) c));
  }

  /** A number already written as JSON text. */
  private record JsonNumber(String text) {}
}
