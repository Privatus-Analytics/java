package com.privatusanalytics.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.privatusanalytics.spring.testapp.TestApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TestApp.class, properties = "PRIVATUS_SITE_ID=pa_FROMENV123")
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class ProdProfileTest {
  @Autowired MockMvc mvc;

  @Test
  void theProdProfileSwitchesTheTagOnWithTheSiteIdFromTheEnvironment() throws Exception {
    assertThat(mvc.perform(get("/")).andReturn().getResponse().getContentAsString())
        .contains("data-site=\"pa_FROMENV123\"");
  }
}
