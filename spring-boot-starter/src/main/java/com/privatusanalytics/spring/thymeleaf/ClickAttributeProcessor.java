package com.privatusanalytics.spring.thymeleaf;

import com.privatusanalytics.ClickAttribute;
import com.privatusanalytics.spring.PrivatusAnalyticsRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * {@code privatus:click="pricing.cta"} becomes {@code data-privatus-click="pricing.cta"}. A name
 * that is not allowed prints nothing, with a warning in the log while the tag is off (so in
 * development).
 */
final class ClickAttributeProcessor extends AbstractAttributeTagProcessor {
  private static final Log LOG = LogFactory.getLog(ClickAttributeProcessor.class);

  private final PrivatusAnalyticsRenderer renderer;

  ClickAttributeProcessor(String prefix, PrivatusAnalyticsRenderer renderer) {
    super(TemplateMode.HTML, prefix, null, false, "click", true, 1000, true);
    this.renderer = renderer;
  }

  @Override
  protected void doProcess(
      ITemplateContext context,
      IProcessableElementTag tag,
      AttributeName attributeName,
      String attributeValue,
      IElementTagStructureHandler structureHandler) {
    Object value = Expressions.textOrExpression(context, attributeValue);
    String name = value == null ? null : value.toString();
    if (ClickAttribute.isValid(name)) {
      structureHandler.setAttribute(ClickAttribute.ATTRIBUTE, name);
    } else if (!renderer.isEnabled()) {
      LOG.warn(
          "privatus:click=\""
              + name
              + "\" was left out. Use 1 to 100 letters, digits, _ . : or - only.");
    }
  }
}
