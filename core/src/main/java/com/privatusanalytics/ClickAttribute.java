package com.privatusanalytics;

import java.util.regex.Pattern;

/**
 * Builds the {@code data-privatus-click} attribute, which gives an element a steady name in click
 * maps.
 *
 * <pre>{@code
 * ClickAttribute.render("pricing.cta") // data-privatus-click="pricing.cta"
 * }</pre>
 */
public final class ClickAttribute {
  /** The attribute name. */
  public static final String ATTRIBUTE = "data-privatus-click";

  /** Allowed names: 1 to 100 letters, digits, and the characters {@code _ . : -}. */
  public static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z0-9_.:-]{1,100}$");

  private ClickAttribute() {}

  /**
   * Whether the name is allowed.
   *
   * @param name the click map name
   * @return true when it matches {@link #VALID_NAME}
   */
  public static boolean isValid(String name) {
    return name != null && VALID_NAME.matcher(name).matches();
  }

  /**
   * The attribute as HTML.
   *
   * @param name the click map name, for example {@code "pricing.cta"}
   * @return the attribute text, or an empty string when the name is not allowed
   */
  public static String render(String name) {
    return isValid(name) ? Html.attribute(ATTRIBUTE, name) : "";
  }
}
