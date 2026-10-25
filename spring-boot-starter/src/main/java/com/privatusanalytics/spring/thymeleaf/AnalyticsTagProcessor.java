package com.privatusanalytics.spring.thymeleaf;

import com.privatusanalytics.spring.PrivatusAnalyticsRenderer;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * {@code <th:block privatus:analytics/>} becomes the tracking script tag. The value may be a map of
 * per-page overrides: {@code privatus:analytics="${ {modules: {'engage','auto'}} }"}.
 */
final class AnalyticsTagProcessor extends AbstractAttributeTagProcessor {
  private final PrivatusAnalyticsRenderer renderer;

  AnalyticsTagProcessor(String prefix, PrivatusAnalyticsRenderer renderer) {
    super(TemplateMode.HTML, prefix, null, false, "analytics", true, 1000, true);
    this.renderer = renderer;
  }

  @Override
  protected void doProcess(
      ITemplateContext context,
      IProcessableElementTag tag,
      AttributeName attributeName,
      String attributeValue,
      IElementTagStructureHandler structureHandler) {
    String html =
        renderer.tag(
            Expressions.currentRequest(),
            Expressions.map(context, attributeValue, "privatus:analytics"));
    if (html.isEmpty()) {
      structureHandler.removeElement();
    } else {
      structureHandler.replaceWith(html, false);
    }
  }
}
