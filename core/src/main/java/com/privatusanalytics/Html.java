package com.privatusanalytics;

/** Small HTML helpers shared by the rest of the library. */
public final class Html {
  private Html() {}

  /**
   * Escapes text for use inside an HTML attribute value or element body. It escapes {@code &},
   * {@code <}, {@code >}, {@code "} and {@code '}.
   *
   * @param value the text, may be null
   * @return the escaped text, or an empty string for null
   */
  public static String escape(String value) {
    if (value == null) {
      return "";
    }
    StringBuilder out = null;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      String replacement =
          switch (c) {
            case '&' -> "&amp;";
            case '<' -> "&lt;";
            case '>' -> "&gt;";
            case '"' -> "&quot;";
            case '\'' -> "&#39;";
            default -> null;
          };
      if (replacement != null) {
        if (out == null) {
          out = new StringBuilder(value.length() + 16);
          out.append(value, 0, i);
        }
        out.append(replacement);
      } else if (out != null) {
        out.append(c);
      }
    }
    return out == null ? value : out.toString();
  }

  /**
   * Writes one attribute as {@code name="value"}, with the value escaped.
   *
   * @param name the attribute name
   * @param value the raw value, or null for a bare attribute such as {@code defer}
   * @return the attribute text
   */
  public static String attribute(String name, String value) {
    return value == null ? name : name + "=\"" + escape(value) + "\"";
  }
}
