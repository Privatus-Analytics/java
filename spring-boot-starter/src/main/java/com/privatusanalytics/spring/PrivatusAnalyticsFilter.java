package com.privatusanalytics.spring;

import com.privatusanalytics.Tag;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds the tag just before {@code </head>} in HTML responses. Off unless {@code
 * privatus.analytics.auto-inject=true}.
 *
 * <p>It only touches responses with status 200, a {@code text/html} content type and no {@code
 * Content-Encoding} header. It skips Ajax requests, pages without a {@code <head>}, and pages that
 * already have the tag. Other responses stream through untouched.
 */
public class PrivatusAnalyticsFilter extends OncePerRequestFilter {
  private static final Pattern HEAD_CLOSE = Pattern.compile("</head>", Pattern.CASE_INSENSITIVE);

  private final PrivatusAnalyticsRenderer renderer;

  /**
   * Creates the filter.
   *
   * @param renderer the renderer
   */
  public PrivatusAnalyticsFilter(PrivatusAnalyticsRenderer renderer) {
    this.renderer = renderer;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !renderer.isEnabled()
        || "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    HtmlBufferingResponse wrapper = new HtmlBufferingResponse(response);
    try {
      chain.doFilter(request, wrapper);
    } catch (IOException | ServletException | RuntimeException e) {
      // Leave the response untouched so the error page can render.
      wrapper.discard();
      throw e;
    }
    if (request.isAsyncStarted()) {
      // The body is written later on another thread, so pass it through as is.
      wrapper.stopBuffering();
      return;
    }
    byte[] body = wrapper.finish();
    if (body == null) {
      return;
    }
    if (wrapper.getStatus() == HttpServletResponse.SC_OK && !wrapper.isEncoded()) {
      body = inject(body, wrapper.charset(), request);
    }
    response.setContentLength(body.length);
    response.getOutputStream().write(body);
  }

  private byte[] inject(byte[] body, Charset charset, HttpServletRequest request) {
    String html = new String(body, charset);
    if (html.contains(Tag.MARKER)) {
      return body;
    }
    Matcher matcher = HEAD_CLOSE.matcher(html);
    if (!matcher.find()) {
      return body;
    }
    String tag = renderer.tag(request);
    if (tag.isEmpty()) {
      return body;
    }
    String result =
        html.substring(0, matcher.start()) + tag + "\n" + html.substring(matcher.start());
    return result.getBytes(charset);
  }
}
