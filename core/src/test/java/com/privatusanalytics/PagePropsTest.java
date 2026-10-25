package com.privatusanalytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PagePropsTest {
  @Test
  void writesJson() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("plan", "pro");
    props.put("Seats", 5);
    props.put("ratio", 0.25);
    props.put("whole", 2.0);
    props.put("beta", true);
    props.put("flags", List.of("new-nav", "dark"));
    props.put("nothing", null);
    props.put("nan", Double.NaN);
    assertEquals(
        "{\"plan\":\"pro\",\"seats\":5,\"ratio\":0.25,\"whole\":2,\"beta\":true,"
            + "\"flags\":[\"new-nav\",\"dark\"]}",
        PageProps.toJson(props));
  }

  @Test
  void isSafeInsideAScript() {
    assertEquals(
        "{\"x\":\"\\u003c/script\\u003e\\u0026\\u2028\\u2029\\\"\\\\\\n\"}",
        PageProps.toJson(Map.of("x", "</script>&\u2028\u2029\"\\\n")));
  }

  @Test
  void keepsAtMost30ListItems() {
    List<Integer> items = new ArrayList<>();
    for (int i = 0; i < 40; i++) {
      items.add(i);
    }
    List<?> list = (List<?>) PageProps.normalize(Map.of("l", items)).get("l");
    assertEquals(30, list.size());
  }

  @Test
  void cutsStringsTo500() {
    assertEquals(
        500, ((String) PageProps.normalize(Map.of("s", "a".repeat(900))).get("s")).length());
  }

  @Test
  void emptyPropsPrintNoScript() {
    assertEquals("", PageProps.script(Map.of(), "n"));
    assertEquals("", PageProps.script(null, null));
    assertEquals("{}", PageProps.toJson(null));
  }

  @Test
  void theScriptDefinesTheFunction() {
    assertEquals(
        "<script>window.privatusPageProps=function(){return {\"plan\":\"pro\"}}</script>",
        PageProps.script(Map.of("plan", "pro"), null));
    assertEquals("{\"plan\":\"pro\"}", PrivatusAnalytics.pagePropsJson(Map.of("plan", "pro")));
  }
}
