package com.privatusanalytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SettingsTest {
  static PrivatusSettings.Builder noEnv() {
    return PrivatusSettings.builder().variables(name -> null);
  }

  @Test
  void siteIdIsNullByDefault() {
    assertNull(noEnv().build().siteId());
    assertNull(noEnv().siteId("  ").build().siteId());
  }

  @Test
  void siteIdFallsBackToTheEnvironmentVariable() {
    Map<String, String> env = Map.of("PRIVATUS_SITE_ID", " pa_FROMENV123 ");
    assertEquals("pa_FROMENV123", noEnv().variables(env::get).build().siteId());
  }

  @Test
  void anExplicitSiteIdWinsOverTheEnvironmentVariable() {
    Map<String, String> env = Map.of("PRIVATUS_SITE_ID", "pa_FROMENV123");
    assertEquals(
        "pa_EXPLICIT01", noEnv().variables(env::get).siteId("pa_EXPLICIT01").build().siteId());
  }

  @Test
  void scriptHostDefaultsAndDropsATrailingSlash() {
    assertEquals("https://privatusstats.com", noEnv().build().scriptHost());
    assertEquals(
        "https://stats.example.com",
        noEnv().scriptHost("https://stats.example.com/").build().scriptHost());
    Map<String, String> env = Map.of("PRIVATUS_SCRIPT_HOST", "https://env.example.com/");
    assertEquals("https://env.example.com", noEnv().variables(env::get).build().scriptHost());
  }

  @Test
  void onlyProductionIsEnabledByDefault() {
    assertTrue(noEnv().environment("production").build().isEnabled());
    assertFalse(noEnv().environment("development").build().isEnabled());
    assertFalse(noEnv().environment("test").build().isEnabled());
    assertFalse(noEnv().build().isEnabled());
  }

  @Test
  void enabledEnvironmentsCanChange() {
    PrivatusSettings settings =
        noEnv()
            .enabledEnvironments(List.of("prod", "staging"))
            .environments(List.of("staging"))
            .build();
    assertTrue(settings.isEnabled());
  }

  @Test
  void enabledForcesTheTagOnOrOff() {
    assertTrue(noEnv().enabled(true).environment("development").build().isEnabled());
    assertFalse(noEnv().enabled(false).environment("production").build().isEnabled());
  }

  @Test
  void toBuilderCopiesEverySetting() {
    PrivatusSettings settings =
        noEnv()
            .siteId("pa_X")
            .modules("engage")
            .excludedPaths("/a")
            .domains("example.com")
            .spa("hash")
            .manual(true)
            .enabled(true)
            .extraAttribute("data-debug", "true")
            .nonce("n")
            .props(Map.of("plan", "pro"))
            .queueStub(true)
            .build();
    assertEquals(settings, settings.toBuilder().build());
  }

  @Test
  void rejectsABadExtraAttributeName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> noEnv().extraAttribute("x\" onload=\"y", "1").build());
  }

  @Test
  void rejectsAnUnknownOverride() {
    assertThrows(IllegalArgumentException.class, () -> noEnv().override(Map.of("bogus", 1)));
  }

  @Test
  void overridePropsAddOnTop() {
    PrivatusSettings settings =
        noEnv()
            .props(Map.of("plan", "pro"))
            .override(Map.of("props", Map.of("role", "admin")))
            .build();
    assertEquals(Map.of("plan", "pro", "role", "admin"), settings.props());
  }
}
