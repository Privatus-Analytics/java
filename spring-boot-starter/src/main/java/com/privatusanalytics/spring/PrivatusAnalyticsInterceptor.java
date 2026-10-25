package com.privatusanalytics.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.SmartView;

/**
 * Puts the {@code privatusAnalytics} helper in the request (for JSP) and in the model (for
 * FreeMarker and other view engines).
 */
public class PrivatusAnalyticsInterceptor implements HandlerInterceptor {
  private final PrivatusAnalyticsRenderer renderer;

  /**
   * Creates the interceptor.
   *
   * @param renderer the renderer
   */
  public PrivatusAnalyticsInterceptor(PrivatusAnalyticsRenderer renderer) {
    this.renderer = renderer;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getAttribute(PrivatusAnalyticsHelper.ATTRIBUTE) == null) {
      request.setAttribute(
          PrivatusAnalyticsHelper.ATTRIBUTE, new PrivatusAnalyticsHelper(renderer, request));
    }
    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView) {
    if (modelAndView == null || modelAndView.wasCleared() || isRedirect(modelAndView)) {
      return;
    }
    if (!modelAndView.getModel().containsKey(PrivatusAnalyticsHelper.ATTRIBUTE)) {
      Object helper = request.getAttribute(PrivatusAnalyticsHelper.ATTRIBUTE);
      modelAndView.addObject(
          PrivatusAnalyticsHelper.ATTRIBUTE,
          helper != null ? helper : new PrivatusAnalyticsHelper(renderer, request));
    }
  }

  private static boolean isRedirect(ModelAndView modelAndView) {
    String viewName = modelAndView.getViewName();
    if (viewName != null) {
      return viewName.startsWith("redirect:") || viewName.startsWith("forward:");
    }
    return modelAndView.getView() instanceof SmartView smart && smart.isRedirectView();
  }
}
