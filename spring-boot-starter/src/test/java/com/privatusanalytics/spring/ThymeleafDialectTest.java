package com.privatusanalytics.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.privatusanalytics.spring.testapp.TestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = TestApp.class,
    properties = {
      "privatus.analytics.site-id=pa_TEST123456",
      "privatus.analytics.enabled=true",
    })
@AutoConfigureMockMvc
class ThymeleafDialectTest {
  static final String SNIPPET =
      "<script defer src=\"https://privatusstats.com/js/pa.js\" data-site=\"pa_TEST123456\">"
          + "</script>";

  @Autowired MockMvc mvc;
  @Autowired ApplicationContext context;

  String body(String path) throws Exception {
    return mvc.perform(get(path))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  void theTagPrintsInTheHead() throws Exception {
    String body = body("/");
    assertThat(body).contains(SNIPPET);
    assertThat(body.indexOf(SNIPPET)).isLessThan(body.indexOf("</head>"));
    assertThat(body)
        .doesNotContain("th:block")
        .doesNotContain("privatus:")
        .doesNotContain("xmlns:");
  }

  @Test
  void eventAttributesOnAButton() throws Exception {
    String body = body("/");
    assertThat(body)
        .containsPattern(
            "<button id=\"export\" type=\"button\"[^>]*data-privatus-event=\"Export CSV\"")
        .contains("data-privatus-prop-format=\"csv\"")
        .contains("data-privatus-prop-source=\"toolbar\"");
  }

  @Test
  void eventAttributesOnAForm() throws Exception {
    assertThat(body("/"))
        .containsPattern("<form id=\"signup\"[^>]*data-privatus-event=\"Signup\"")
        .contains("data-privatus-prop-plan=\"pro\"")
        .contains("data-privatus-prop-seats=\"3\"");
  }

  @Test
  void eventNamesFromAnExpressionAreEscaped() throws Exception {
    assertThat(body("/"))
        .contains("<button id=\"dynamic\" data-privatus-event=\"Report &quot;exported&quot;\">");
  }

  @Test
  void clickMapNames() throws Exception {
    String body = body("/");
    assertThat(body)
        .contains("<a id=\"cta\" href=\"/pricing\" data-privatus-click=\"pricing.cta\">");
    assertThat(body).contains("<a id=\"bad\" href=\"/x\">");
  }

  @Test
  void aStrayPropsAttributeIsRemoved() throws Exception {
    assertThat(body("/")).contains("<span id=\"stray\">Stray</span>");
  }

  @Test
  void usesTheNonceFromTheRequest() throws Exception {
    String body =
        mvc.perform(get("/").requestAttr("cspNonce", "abc123"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(body).contains("data-site=\"pa_TEST123456\" nonce=\"abc123\"></script>");
  }

  @Test
  void pagePropsFromAProviderBean() throws Exception {
    String body = body("/?plan=pro");
    assertThat(body)
        .contains(
            "<script>window.privatusPageProps=function(){return {\"plan\":\"pro\"}}</script>\n"
                + "<script defer src=\"https://privatusstats.com/js/pa.js\""
                + " data-site=\"pa_TEST123456\" data-props=\"privatusPageProps\"></script>");
  }

  @Test
  void perPageOverrides() throws Exception {
    String body = body("/overrides?plan=pro");
    assertThat(body).contains("data-modules=\"engage,auto\"");
    assertThat(body).contains("{\"plan\":\"pro\",\"role\":\"admin\"}");
  }

  @Test
  void theHelperIsInTheModelAndTheRequest() throws Exception {
    String body =
        mvc.perform(get("/helper"))
            .andExpect(model().attributeExists("privatusAnalytics"))
            .andExpect(
                request().attribute("privatusAnalytics", org.hamcrest.Matchers.notNullValue()))
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(body).contains("<div id=\"helper\">" + SNIPPET + "</div>");
  }

  @Test
  void worksWithThUnless() throws Exception {
    assertThat(body("/staff?admin=false")).contains(SNIPPET);
    assertThat(body("/staff?admin=true")).doesNotContain("pa.js");
  }

  @Test
  void theFilterIsOffByDefault() {
    assertThat(context.containsBean("privatusAnalyticsFilter")).isFalse();
  }

  @Test
  void theFilterDoesNothingWhenOff() throws Exception {
    assertThat(body("/plain")).doesNotContain("pa.js");
  }
}
