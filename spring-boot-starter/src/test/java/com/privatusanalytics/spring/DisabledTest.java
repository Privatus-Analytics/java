package com.privatusanalytics.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.privatusanalytics.spring.testapp.TestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** No prod profile, so the tag stays off, but event attributes still print. */
@SpringBootTest(
    classes = TestApp.class,
    properties = {
      "privatus.analytics.site-id=pa_TEST123456",
      "privatus.analytics.auto-inject=true",
    })
@AutoConfigureMockMvc
class DisabledTest {
  @Autowired MockMvc mvc;

  String body(String path) throws Exception {
    return mvc.perform(get(path)).andReturn().getResponse().getContentAsString();
  }

  @Test
  void theTagPrintsNothingOutsideTheProdProfile() throws Exception {
    String body = body("/?plan=pro");
    assertThat(body).doesNotContain("pa.js").doesNotContain("privatusPageProps");
    assertThat(body).doesNotContain("th:block").doesNotContain("privatus:");
  }

  @Test
  void eventAndClickAttributesStillPrint() throws Exception {
    String body = body("/");
    assertThat(body).contains("data-privatus-event=\"Export CSV\"");
    assertThat(body).contains("data-privatus-click=\"pricing.cta\"");
  }

  @Test
  void theFilterAddsNothing() throws Exception {
    assertThat(body("/plain"))
        .isEqualTo("<html><head><title>Plain</title></head><body>Hi</body></html>");
  }
}
