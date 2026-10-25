package com.privatusanalytics.spring.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Removes a {@code privatus:props} attribute left without a {@code privatus:event}, so it never
 * shows up in the page. The event processor reads it when both are present.
 */
final class PropsAttributeProcessor extends AbstractAttributeTagProcessor {
  PropsAttributeProcessor(String prefix) {
    super(TemplateMode.HTML, prefix, null, false, EventAttributeProcessor.PROPS, true, 1100, true);
  }

  @Override
  protected void doProcess(
      ITemplateContext context,
      IProcessableElementTag tag,
      AttributeName attributeName,
      String attributeValue,
      IElementTagStructureHandler structureHandler) {
    // Nothing to print. The attribute is removed by the base class.
  }
}
