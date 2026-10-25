package com.privatusanalytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EventAttributesTest {
  @Test
  void printsTheEventAndItsProps() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("format", "csv");
    props.put("source", "toolbar");
    assertEquals(
        "data-privatus-event=\"Export CSV\" data-privatus-prop-format=\"csv\""
            + " data-privatus-prop-source=\"toolbar\"",
        EventAttributes.render("Export CSV", props));
  }

  @Test
  void anEventWithoutProps() {
    assertEquals("data-privatus-event=\"Signup\"", PrivatusAnalytics.eventAttrs("Signup"));
  }

  @Test
  void anEmptyNamePrintsNothing() {
    assertEquals("", EventAttributes.render("   ", Map.of("a", "b")));
    assertEquals("", EventAttributes.render(null, null));
  }

  @Test
  void trimsAndCutsTheName() {
    assertEquals("data-privatus-event=\"Hi\"", EventAttributes.render("  Hi  "));
    String longName = "x".repeat(200);
    assertEquals(
        120, EventAttributes.attributes(longName, null).get("data-privatus-event").length());
  }

  @Test
  void cleansPropKeys() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("Plan Type", "pro");
    props.put("a-b.c", "1");
    props.put("k".repeat(80), "long");
    Map<String, String> attrs = EventAttributes.attributes("E", props);
    assertTrue(attrs.containsKey("data-privatus-prop-plan_type"));
    assertTrue(attrs.containsKey("data-privatus-prop-a_b_c"));
    assertTrue(attrs.containsKey("data-privatus-prop-" + "k".repeat(64)));
  }

  @Test
  void turnsValuesIntoText() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("yes", true);
    props.put("no", false);
    props.put("count", 3);
    props.put("price", 9.5);
    props.put("big", new BigDecimal("10.00"));
    props.put("tags", List.of("a", "b"));
    props.put("array", new String[] {"x", "y"});
    props.put("gone", null);
    Map<String, String> clean = EventAttributes.props(props);
    assertEquals("true", clean.get("yes"));
    assertEquals("false", clean.get("no"));
    assertEquals("3", clean.get("count"));
    assertEquals("9.5", clean.get("price"));
    assertEquals("10", clean.get("big"));
    assertEquals("a,b", clean.get("tags"));
    assertEquals("x,y", clean.get("array"));
    assertFalse(clean.containsKey("gone"));
  }

  @Test
  void cutsValuesTo500Characters() {
    assertEquals(500, EventAttributes.props(Map.of("v", "z".repeat(600))).get("v").length());
  }

  @Test
  void keepsAtMost30Props() {
    Map<String, Object> props = new LinkedHashMap<>();
    for (int i = 0; i < 40; i++) {
      props.put("p" + i, i);
    }
    assertEquals(30, EventAttributes.props(props).size());
    assertTrue(EventAttributes.props(props).containsKey("p29"));
  }

  @Test
  void escapesEverything() {
    String html = EventAttributes.render("<b>\"x\"</b>", Map.of("q", "a&b'c"));
    assertEquals(
        "data-privatus-event=\"&lt;b&gt;&quot;x&quot;&lt;/b&gt;\""
            + " data-privatus-prop-q=\"a&amp;b&#39;c\"",
        html);
  }

  @Test
  void nullItemsInListsAreDropped() {
    assertEquals("a,b", EventAttributes.props(Map.of("l", Arrays.asList("a", null, "b"))).get("l"));
  }

  @Test
  void clickAttr() {
    assertEquals("data-privatus-click=\"pricing.cta\"", PrivatusAnalytics.clickAttr("pricing.cta"));
    assertEquals("data-privatus-click=\"a_b:c-d\"", ClickAttribute.render("a_b:c-d"));
    assertEquals("", ClickAttribute.render("has space"));
    assertEquals("", ClickAttribute.render("x\"y"));
    assertEquals("", ClickAttribute.render(""));
    assertEquals("", ClickAttribute.render(null));
    assertEquals("", ClickAttribute.render("a".repeat(101)));
    assertTrue(ClickAttribute.isValid("a".repeat(100)));
  }
}
