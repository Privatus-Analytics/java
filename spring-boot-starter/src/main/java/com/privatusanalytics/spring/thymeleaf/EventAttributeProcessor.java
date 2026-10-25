package com.privatusanalytics.spring.thymeleaf;

import com.privatusanalytics.EventAttributes;
import com.privatusanalytics.Html;
import java.util.Map;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * {@code privatus:event="Export CSV" privatus:props="${ {format:'csv'} }"} becomes {@code
 * data-privatus-event} and {@code data-privatus-prop-*} attributes. The event name may also be an
 * expression such as {@code ${eventName}}.
 */
final class EventAttributeProcessor extends AbstractAttributeTagProcessor {
  static final String PROPS = "props";

  EventAttributeProcessor(String prefix) {
    super(TemplateMode.HTML, prefix, null, false, "event", true, 1000, true);
  }

  @Override
  protected void doProcess(
      ITemplateContext context,
      IProcessableElementTag tag,
      AttributeName attributeName,
      String attributeValue,
      IElementTagStructureHandler structureHandler) {
    String prefix = getDialectPrefix();
    Map<?, ?> props = null;
    if (tag.hasAttribute(prefix, PROPS)) {
      props = Expressions.map(context, tag.getAttributeValue(prefix, PROPS), "privatus:props");
      structureHandler.removeAttribute(prefix, PROPS);
    }
    Object name = Expressions.textOrExpression(context, attributeValue);
    Map<String, String> attributes =
        EventAttributes.attributes(name == null ? null : name.toString(), props);
    // Thymeleaf writes attribute values as given, so escape them here.
    attributes.forEach((key, value) -> structureHandler.setAttribute(key, Html.escape(value)));
  }
}
