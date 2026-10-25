package com.privatusanalytics.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.privatusanalytics.QueueStub;
import com.privatusanalytics.spring.testapp.TestApp;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/** No prod profile, so the tag stays off, but event attributes still print. */
@SpringBootTest(
    classes = TestApp.class,
    properties = {
      "privatus.analytics.site-id=pa_TEST123456",
      "privatus.analytics.auto-inject=true",
    })
@AutoConfigureMockMvc
@Import(DisabledTest.CountingProps.class)
class DisabledTest {
  @Autowired MockMvc mvc;

  static final AtomicInteger PROPS_CALLS = new AtomicInteger();

  @TestConfiguration
  static class CountingProps {
    @Bean
    PrivatusPagePropsProvider countingProps() {
      return request -> {
        PROPS_CALLS.incrementAndGet();
        return Map.of("tier", "gold");
      };
    }
  }

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

  @Test
  void withTheQueueStubOnlyTheStubPrintsAndNoPropsProviderRuns() throws Exception {
    PROPS_CALLS.set(0);
    String body =
        mvc.perform(get("/stub").requestAttr("cspNonce", "n1"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(body)
        .contains(
            "<meta name=\"start\"><script nonce=\"n1\">"
                + QueueStub.CODE
                + "</script><meta name=\"end\">");
    assertThat(body).contains("<meta name=\"plain-start\"><meta name=\"plain-end\">");
    assertThat(body).doesNotContain("pa.js").doesNotContain("privatusPageProps");
    assertThat(PROPS_CALLS.get()).isZero();
  }
}
