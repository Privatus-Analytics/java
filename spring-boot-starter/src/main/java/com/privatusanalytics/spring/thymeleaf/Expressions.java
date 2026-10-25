package com.privatusanalytics.spring.thymeleaf;

import java.util.Map;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.standard.expression.StandardExpressions;

/** Helpers shared by the processors. */
final class Expressions {
  private Expressions() {}

  /** True when the text is a Thymeleaf expression rather than plain text. */
  static boolean isExpression(String value) {
    String v = value.strip();
    return v.contains("${")
        || v.contains("*{")
        || v.contains("#{")
        || v.contains("@{")
        || v.contains("~{")
        || v.startsWith("'")
        || v.startsWith("|");
  }

  /** Evaluates the value when it is an expression, else returns it as text. */
  static Object textOrExpression(ITemplateContext context, String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return isExpression(value) ? evaluate(context, value) : value;
  }

  /** Evaluates an expression. */
  static Object evaluate(ITemplateContext context, String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return StandardExpressions.getExpressionParser(context.getConfiguration())
        .parseExpression(context, value)
        .execute(context);
  }

  /** Evaluates an expression that must give a map (or nothing). */
  static Map<?, ?> map(ITemplateContext context, String value, String attribute) {
    Object result = evaluate(context, value);
    if (result == null) {
      return null;
    }
    if (result instanceof Map<?, ?> map) {
      return map;
    }
    throw new TemplateProcessingException(
        attribute + " must be a map, for example ${ {format:'csv'} }, not: " + value);
  }

  /** The current servlet request, when there is one. */
  static jakarta.servlet.http.HttpServletRequest currentRequest() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
  }
}
