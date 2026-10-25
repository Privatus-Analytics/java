package com.privatusanalytics;

/**
 * The official queue stub. Put it before the tracker so calls such as {@code privatus.track(...)}
 * made before the tracker loads are kept and sent once it is ready.
 */
public final class QueueStub {
  /** The stub code, without the script element. */
  public static final String CODE =
      "window.privatus=window.privatus||{q:[]};"
          + "['track','pageview','props','optOut','optIn','ready'].forEach(function(m){"
          + "window.privatus[m]=window.privatus[m]||function(){"
          + "window.privatus.q.push([m,Array.prototype.slice.call(arguments)])}})";

  private QueueStub() {}

  /**
   * The stub inside a script element.
   *
   * @param nonce a Content Security Policy nonce, may be null
   * @return the script element
   */
  public static String script(String nonce) {
    return PageProps.openScript(nonce) + CODE + "</script>";
  }
}
