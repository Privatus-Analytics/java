package com.privatusanalytics.spring.thymeleaf;

import com.privatusanalytics.spring.PrivatusAnalyticsRenderer;
import java.util.Set;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * The {@code privatus:} Thymeleaf dialect.
 *
 * <pre>{@code
 * <html xmlns:privatus="https://privatusanalytics.com/thymeleaf">
 * <head><th:block privatus:analytics/></head>
 * <button privatus:event="Export CSV" privatus:props="${ {format:'csv'} }">Export</button>
 * <a href="/signup" privatus:click="pricing.cta">Start</a>
 * }</pre>
 */
public class PrivatusDialect extends AbstractProcessorDialect {
  /** The attribute prefix. */
  public static final String PREFIX = "privatus";

  private final PrivatusAnalyticsRenderer renderer;

  /**
   * Creates the dialect.
   *
   * @param renderer the renderer
   */
  public PrivatusDialect(PrivatusAnalyticsRenderer renderer) {
    super("Privatus Analytics", PREFIX, StandardDialect.PROCESSOR_PRECEDENCE);
    this.renderer = renderer;
  }

  @Override
  public Set<IProcessor> getProcessors(String dialectPrefix) {
    return Set.of(
        new AnalyticsTagProcessor(dialectPrefix, renderer),
        new EventAttributeProcessor(dialectPrefix),
        new PropsAttributeProcessor(dialectPrefix),
        new ClickAttributeProcessor(dialectPrefix, renderer),
        // Removes xmlns:privatus from the page, like Thymeleaf does for xmlns:th.
        new StandardXmlNsTagProcessor(TemplateMode.HTML, dialectPrefix));
  }
}
