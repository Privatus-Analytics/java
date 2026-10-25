package com.privatusanalytics.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.privatusanalytics.spring.testapp.TestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = TestApp.class,
    properties = {
      "privatus.analytics.site-id=pa_TEST123456",
      "privatus.analytics.enabled=true",
      "privatus.analytics.auto-inject=true",
    })
@AutoConfigureMockMvc
class AutoInjectTest {
  static final String SNIPPET = ThymeleafDialectTest.SNIPPET;

  @Autowired MockMvc mvc;

  @Test
  void addsTheTagToTheHead() throws Exception {
    MockHttpServletResponse response = mvc.perform(get("/plain")).andReturn().getResponse();
    String body = response.getContentAsString();
    assertThat(body).contains(SNIPPET + "\n</head>");
    assertThat(response.getContentLength()).isEqualTo(response.getContentAsByteArray().length);
  }

  @Test
  void usesTheNonceAndPagePropsToo() throws Exception {
    String body =
        mvc.perform(get("/plain?plan=pro").requestAttr("cspNonce", "n1"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(body).contains("<script nonce=\"n1\">window.privatusPageProps=");
    assertThat(body).contains("data-props=\"privatusPageProps\" nonce=\"n1\"></script>\n</head>");
  }

  @Test
  void doesNotAddASecondTagWhenTheLayoutHasOne() throws Exception {
    String body = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();
    assertThat(body.split("/js/pa\\.js", -1)).hasSize(2);
  }

  @Test
  void leavesJsonAlone() throws Exception {
    assertThat(mvc.perform(get("/data")).andReturn().getResponse().getContentAsString())
        .doesNotContain("pa.js");
  }

  @Test
  void leavesHtmlWithoutAHeadAlone() throws Exception {
    assertThat(mvc.perform(get("/bare")).andReturn().getResponse().getContentAsString())
        .isEqualTo("<p>No head here</p>");
  }

  @Test
  void leavesAjaxRequestsAlone() throws Exception {
    assertThat(
            mvc.perform(get("/plain").header("X-Requested-With", "XMLHttpRequest"))
                .andReturn()
                .getResponse()
                .getContentAsString())
        .doesNotContain("pa.js");
  }

  @Test
  void leavesOtherStatusCodesAlone() throws Exception {
    MockHttpServletResponse response = mvc.perform(get("/missing")).andReturn().getResponse();
    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(response.getContentAsString()).doesNotContain("pa.js").contains("</head>");
  }
}
