package com.privatusanalytics;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Shared rules for prop keys and values. */
final class Values {
  static final int MAX_KEY_LENGTH = 64;
  static final int MAX_PROPS = 30;
  static final int MAX_VALUE_LENGTH = 500;
  static final int MAX_LIST_ITEMS = 30;

  private static final Pattern NOT_KEY_CHAR = Pattern.compile("[^a-z0-9_]");
  private static final Pattern JSON_NUMBER =
      Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?");

  private Values() {}

  /** Lowercases the key, turns other characters into underscores and cuts it to 64. */
  static String normalizeKey(Object key) {
    if (key == null) {
      return "";
    }
    String lower = key.toString().toLowerCase(Locale.ROOT);
    return truncate(NOT_KEY_CHAR.matcher(lower).replaceAll("_"), MAX_KEY_LENGTH);
  }

  /** Cuts text to at most {@code max} characters without splitting a surrogate pair. */
  static String truncate(String value, int max) {
    if (value.length() <= max) {
      return value;
    }
    int end = max;
    if (Character.isHighSurrogate(value.charAt(end - 1))) {
      end--;
    }
    return value.substring(0, end);
  }

  /** True for a boolean value. */
  static boolean isBoolean(Object value) {
    return value instanceof Boolean;
  }

  /** True for a list, set or array. */
  static boolean isList(Object value) {
    return value instanceof Collection<?> || (value != null && value.getClass().isArray());
  }

  /** The items of a list, set or array, with nulls removed. */
  static List<Object> items(Object value) {
    List<Object> items = new ArrayList<>();
    if (value instanceof Collection<?> collection) {
      for (Object item : collection) {
        if (item != null) {
          items.add(item);
        }
      }
    } else if (value != null && value.getClass().isArray()) {
      int length = Array.getLength(value);
      for (int i = 0; i < length; i++) {
        Object item = Array.get(value, i);
        if (item != null) {
          items.add(item);
        }
      }
    }
    return items;
  }

  /**
   * Writes a number as plain JSON text, or returns null when it has no JSON form (NaN and
   * infinity).
   */
  static String numberText(Number number) {
    if (number instanceof Double || number instanceof Float) {
      double d = number.doubleValue();
      if (Double.isNaN(d) || Double.isInfinite(d)) {
        return null;
      }
      BigDecimal decimal =
          number instanceof Float
              ? new BigDecimal(Float.toString(number.floatValue()))
              : BigDecimal.valueOf(d);
      return plain(decimal);
    }
    if (number instanceof BigDecimal decimal) {
      return plain(decimal);
    }
    if (number instanceof BigInteger
        || number instanceof Long
        || number instanceof Integer
        || number instanceof Short
        || number instanceof Byte) {
      return number.toString();
    }
    String text = number.toString();
    return JSON_NUMBER.matcher(text).matches() ? text : null;
  }

  private static String plain(BigDecimal decimal) {
    if (decimal.signum() == 0) {
      return "0";
    }
    return decimal.stripTrailingZeros().toPlainString();
  }

  /** The plain text form of one value, used for HTML attributes. */
  static String text(Object value) {
    if (value instanceof Boolean bool) {
      return bool ? "true" : "false";
    }
    if (value instanceof Number number) {
      String text = numberText(number);
      return text == null ? number.toString() : text;
    }
    return String.valueOf(value);
  }
}
