package com.privatusanalytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TagTest {
  static final String SNIPPET =
      "<script defer src=\"https://privatusstats.com/js/pa.js\" data-site=\"pa_TEST123456\">"
          + "</script>";

  static PrivatusSettings.Builder base() {
    return PrivatusSettings.builder().variables(name -> null).siteId("pa_TEST123456").enabled(true);
  }

  @Test
  void printsTheBasicSnippet() {
    assertEquals(SNIPPET, Tag.render(base().build()));
  }

  @Test
  void printsNothingWithoutASiteId() {
    assertEquals("", Tag.render(base().siteId(null).build()));
  }

  @Test
  void printsNothingOutsideTheEnabledEnvironments() {
    assertEquals("", Tag.render(base().enabled(null).build()));
    assertEquals("", Tag.render(base().enabled(null).environment("development").build()));
    assertEquals(SNIPPET, Tag.render(base().enabled(null).environment("production").build()));
  }

  @Test
  void offWithTheQueueStubPrintsOnlyTheStub() {
    String stub = "<script nonce=\"abc\">" + QueueStub.CODE + "</script>";
    assertEquals(
        stub,
        Tag.render(
            base()
                .enabled(false)
                .queueStub(true)
                .nonce("abc")
                .props(Map.of("plan", "pro"))
                .build()));
    assertEquals(
        "<script>" + QueueStub.CODE + "</script>",
        Tag.render(base().enabled(null).environment("development").queueStub(true).build()));
  }

  @Test
  void offWithoutTheQueueStubPrintsNothing() {
    assertEquals(
        "",
        Tag.render(
            base()
                .enabled(false)
                .queueStub(false)
                .nonce("abc")
                .props(Map.of("plan", "pro"))
                .build()));
  }

  @Test
  void noSiteIdWithTheQueueStubPrintsOnlyTheStub() {
    assertEquals(
        "<script nonce=\"abc\">" + QueueStub.CODE + "</script>",
        Tag.render(
            base().siteId(null).queueStub(true).nonce("abc").props(Map.of("plan", "pro")).build()));
  }

  @Test
  void addsModulesAndExclusions() {
    String tag =
        Tag.render(
            base().modules("engage", "auto").excludedPaths("/admin/*", "/preview/*").build());
    assertTrue(tag.contains("data-modules=\"engage,auto\""));
    assertTrue(tag.contains("data-exclude=\"/admin/*,/preview/*\""));
  }

  @Test
  void anEmptyModulesListLoadsNone() {
    assertTrue(Tag.render(base().modules().build()).contains("data-modules=\"\""));
    assertFalse(Tag.render(base().modules((List<String>) null).build()).contains("data-modules"));
  }

  @Test
  void addsDomainsSpaModeAndManualMode() {
    String tag =
        Tag.render(base().domains("example.com, www.example.com").spa("hash").manual(true).build());
    assertTrue(tag.contains("data-domains=\"example.com,www.example.com\""));
    assertTrue(tag.contains("data-spa=\"hash\""));
    assertTrue(tag.contains("data-manual=\"true\""));
  }

  @Test
  void ignoresAnUnknownSpaModeAndTheAutoDefault() {
    assertFalse(Tag.render(base().spa("bogus").build()).contains("data-spa"));
    assertFalse(Tag.render(base().spa("auto").build()).contains("data-spa"));
  }

  @Test
  void escapesAttributeValues() {
    assertTrue(
        Tag.render(base().excludedPaths("/a\"b").build()).contains("data-exclude=\"/a&quot;b\""));
  }

  @Test
  void perCallOverridesWinOverTheSettings() {
    PrivatusSettings settings = base().modules("engage").build();
    String tag = Tag.render(settings.toBuilder().modules("vitals").build());
    assertTrue(tag.contains("data-modules=\"vitals\""));
    assertTrue(Tag.render(settings).contains("data-modules=\"engage\""));
  }

  @Test
  void overridesFromAMap() {
    Map<String, Object> overrides = new LinkedHashMap<>();
    overrides.put("modules", List.of("engage", "auto"));
    overrides.put("excluded-paths", "/preview/*");
    overrides.put("spa", "history");
    overrides.put("nonce", "n1");
    String tag = Tag.render(base().override(overrides).build());
    assertTrue(tag.contains("data-modules=\"engage,auto\""));
    assertTrue(tag.contains("data-exclude=\"/preview/*\""));
    assertTrue(tag.contains("data-spa=\"history\""));
    assertTrue(tag.contains("nonce=\"n1\""));
  }

  @Test
  void addsExtraAttributesAndANonce() {
    String tag =
        Tag.render(
            base().extraAttribute("data-mask", "/orders/*->/orders/:id").nonce("abc123").build());
    assertTrue(tag.contains("data-mask=\"/orders/*-&gt;/orders/:id\""));
    assertTrue(tag.contains("nonce=\"abc123\""));
  }

  @Test
  void pagePropsPrintAScriptBeforeTheTag() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("plan", "pro");
    props.put("role", "admin");
    String html = Tag.render(base().props(props).nonce("n1").build());
    assertEquals(
        "<script nonce=\"n1\">window.privatusPageProps=function(){return"
            + " {\"plan\":\"pro\",\"role\":\"admin\"}}</script>\n"
            + "<script defer src=\"https://privatusstats.com/js/pa.js\" data-site=\"pa_TEST123456\""
            + " data-props=\"privatusPageProps\" nonce=\"n1\"></script>",
        html);
  }

  @Test
  void emptyPagePropsAddNothing() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("plan", null);
    assertEquals(SNIPPET, Tag.render(base().props(props).build()));
  }

  @Test
  void theQueueStubComesFirst() {
    String html = Tag.render(base().queueStub(true).props(Map.of("plan", "pro")).build());
    String[] lines = html.split("\n");
    assertEquals(3, lines.length);
    assertEquals(QueueStub.script(null), lines[0]);
    assertTrue(lines[1].startsWith("<script>window.privatusPageProps="));
    assertTrue(lines[2].contains("/js/pa.js"));
  }

  @Test
  void theQueueStubIsTheOfficialOne() {
    assertEquals(
        "<script nonce=\"x\">window.privatus=window.privatus||{q:[]};"
            + "['track','pageview','props','optOut','optIn','ready'].forEach(function(m){"
            + "window.privatus[m]=window.privatus[m]||function(){"
            + "window.privatus.q.push([m,Array.prototype.slice.call(arguments)])}})</script>",
        QueueStub.script("x"));
  }

  @Test
  void disabledPrintsNoPropsEvenWithTheStub() {
    assertEquals(
        "<script>" + QueueStub.CODE + "</script>",
        Tag.render(base().enabled(false).queueStub(true).props(Map.of("plan", "pro")).build()));
  }
}
