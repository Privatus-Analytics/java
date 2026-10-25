package com.privatusanalytics.spring;

import static org.assertj.core.api.Assertions.assertThat;

import com.privatusanalytics.PrivatusSettings;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/** The README examples for FreeMarker, run through FreeMarker itself. */
class FreeMarkerHelperTest {
  @Test
  void theReadmeExamplesWork() throws Exception {
    PrivatusAnalyticsRenderer renderer =
        new PrivatusAnalyticsRenderer(
            PrivatusSettings.builder()
                .variables(name -> null)
                .siteId("pa_TEST123456")
                .enabled(true)
                .build(),
            "cspNonce",
            List.of());
    Configuration config = new Configuration(Configuration.VERSION_2_3_32);
    config.setOutputFormat(HTMLOutputFormat.INSTANCE);
    Template template =
        new Template(
            "page",
            new StringReader(
                "${privatusAnalytics.tag?no_esc}\n"
                    + "<button ${privatusAnalytics.eventAttrs(\"Export CSV\", {\"format\": \"csv\"})?no_esc}>"
                    + "<a ${privatusAnalytics.clickAttr(\"pricing.cta\")?no_esc}>\n"
                    + "${privatusAnalytics.tagWith({\"modules\": [\"engage\", \"auto\"]})?no_esc}"),
            config);
    StringWriter out = new StringWriter();
    template.process(
        Map.of(
            "privatusAnalytics",
            new PrivatusAnalyticsHelper(renderer, new MockHttpServletRequest())),
        out);
    assertThat(out.toString())
        .isEqualTo(
            ThymeleafDialectTest.SNIPPET
                + "\n<button data-privatus-event=\"Export CSV\" data-privatus-prop-format=\"csv\">"
                + "<a data-privatus-click=\"pricing.cta\">\n"
                + "<script defer src=\"https://privatusstats.com/js/pa.js\" data-site=\"pa_TEST123456\""
                + " data-modules=\"engage,auto\"></script>");
  }
}
