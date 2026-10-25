package com.privatusanalytics.spring.testapp;

import com.privatusanalytics.spring.PrivatusPagePropsProvider;
import java.util.Map;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApp {
  @Bean
  PrivatusPagePropsProvider planProps() {
    return request -> {
      String plan = request.getParameter("plan");
      return plan == null ? null : Map.of("plan", plan);
    };
  }
}
