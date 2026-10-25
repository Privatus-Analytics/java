package com.privatusanalytics.spring.testapp;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PagesController {
  static final String PLAIN = "<html><head><title>Plain</title></head><body>Hi</body></html>";

  @GetMapping("/")
  String home(Model model) {
    model.addAttribute("eventName", "Report \"exported\"");
    return "home";
  }

  @GetMapping("/overrides")
  String overrides() {
    return "overrides";
  }

  @GetMapping("/staff")
  String staff(Model model, @org.springframework.web.bind.annotation.RequestParam boolean admin) {
    model.addAttribute("admin", admin);
    return "staff";
  }

  @GetMapping("/helper")
  String helper() {
    return "helper";
  }

  @GetMapping(value = "/plain", produces = MediaType.TEXT_HTML_VALUE)
  @ResponseBody
  String plain() {
    return PLAIN;
  }

  @GetMapping(value = "/bare", produces = MediaType.TEXT_HTML_VALUE)
  @ResponseBody
  String bare() {
    return "<p>No head here</p>";
  }

  @GetMapping("/data")
  @ResponseBody
  Map<String, Object> data() {
    return Map.of("ok", true, "html", "</head>");
  }

  @GetMapping("/missing")
  ResponseEntity<String> missing() {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_HTML).body(PLAIN);
  }
}
