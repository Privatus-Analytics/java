package com.privatusanalytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Settings for the tracking script. Build them once with {@link #builder()}, and use {@link
 * #toBuilder()} to change a setting for one page.
 *
 * <pre>{@code
 * PrivatusSettings settings = PrivatusSettings.builder()
 *     .siteId("pa_YOURSITEID")       // or the PRIVATUS_SITE_ID environment variable
 *     .environment("production")
 *     .build();
 * }</pre>
 *
 * <p>Instances never change, so one can be shared by every request.
 */
public final class PrivatusSettings {
  /** Where {@code /js/pa.js} is served unless you change it. */
  public static final String DEFAULT_SCRIPT_HOST = "https://privatusstats.com";

  /** Environment variable read when no site id is set. */
  public static final String SITE_ID_VARIABLE = "PRIVATUS_SITE_ID";

  /** Environment variable read when no script host is set. */
  public static final String SCRIPT_HOST_VARIABLE = "PRIVATUS_SCRIPT_HOST";

  /** The tracker modules. */
  public static final List<String> MODULES = List.of("engage", "auto", "vitals", "clicks", "ask");

  /** The single-page app modes. */
  public static final List<String> SPA_MODES = List.of("auto", "history", "hash", "off");

  /** The environments that get the tag by default. */
  public static final List<String> DEFAULT_ENABLED_ENVIRONMENTS = List.of("production");

  private static final Pattern ATTRIBUTE_NAME = Pattern.compile("[A-Za-z_:][-A-Za-z0-9_:.]*");

  private final String siteId;
  private final String scriptHost;
  private final List<String> modules;
  private final List<String> excludedPaths;
  private final List<String> domains;
  private final String spa;
  private final boolean manual;
  private final Boolean enabled;
  private final List<String> enabledEnvironments;
  private final List<String> environments;
  private final Map<String, String> extraAttributes;
  private final String nonce;
  private final Map<String, Object> props;
  private final boolean queueStub;

  private PrivatusSettings(Builder builder) {
    Function<String, String> lookup = builder.variables;
    this.siteId = resolve(builder.siteId, lookup, SITE_ID_VARIABLE);
    String host = resolve(builder.scriptHost, lookup, SCRIPT_HOST_VARIABLE);
    host = host == null ? DEFAULT_SCRIPT_HOST : host;
    this.scriptHost = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
    this.modules = cleanList(builder.modules);
    List<String> excluded = cleanList(builder.excludedPaths);
    this.excludedPaths = excluded == null ? List.of() : excluded;
    this.domains = cleanList(builder.domains);
    this.spa = builder.spa == null ? null : builder.spa.strip().toLowerCase(Locale.ROOT);
    this.manual = builder.manual;
    this.enabled = builder.enabled;
    List<String> enabledList = cleanList(builder.enabledEnvironments);
    this.enabledEnvironments = enabledList == null ? List.of() : enabledList;
    List<String> active = cleanList(builder.environments);
    this.environments = active == null ? List.of() : active;
    Map<String, String> extras = new LinkedHashMap<>();
    builder.extraAttributes.forEach(
        (name, value) -> {
          if (name == null || !ATTRIBUTE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Not a valid HTML attribute name: " + name);
          }
          if (value != null) {
            extras.put(name, value);
          }
        });
    this.extraAttributes = Collections.unmodifiableMap(extras);
    this.nonce = builder.nonce == null || builder.nonce.isBlank() ? null : builder.nonce;
    this.props = Collections.unmodifiableMap(new LinkedHashMap<>(builder.props));
    this.queueStub = builder.queueStub;
  }

  /**
   * Starts a new set of settings with the defaults.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder that starts from these settings. Handy for one-page changes.
   *
   * @return a builder holding a copy of every setting
   */
  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.siteId = siteId;
    builder.scriptHost = scriptHost;
    builder.modules = modules;
    builder.excludedPaths = excludedPaths;
    builder.domains = domains;
    builder.spa = spa;
    builder.manual = manual;
    builder.enabled = enabled;
    builder.enabledEnvironments = enabledEnvironments;
    builder.environments = environments;
    builder.extraAttributes = new LinkedHashMap<>(extraAttributes);
    builder.nonce = nonce;
    builder.props = new LinkedHashMap<>(props);
    builder.queueStub = queueStub;
    return builder;
  }

  /**
   * The site id (it starts with {@code pa_}).
   *
   * @return the site id, or null when none is set
   */
  public String siteId() {
    return siteId;
  }

  /**
   * Where {@code /js/pa.js} is served, without a trailing slash.
   *
   * @return the script host
   */
  public String scriptHost() {
    return scriptHost;
  }

  /**
   * Tracker modules to load.
   *
   * @return the modules, or null for the tracker default
   */
  public List<String> modules() {
    return modules;
  }

  /**
   * Path globs the tracker never counts.
   *
   * @return the paths, never null
   */
  public List<String> excludedPaths() {
    return excludedPaths;
  }

  /**
   * Hostnames to count.
   *
   * @return the hostnames, or null to count every host
   */
  public List<String> domains() {
    return domains;
  }

  /**
   * The single-page app mode.
   *
   * @return auto, history, hash, off, or null for the tracker default
   */
  public String spa() {
    return spa;
  }

  /**
   * Whether the automatic pageview is off.
   *
   * @return true for manual mode
   */
  public boolean manual() {
    return manual;
  }

  /**
   * The forced on or off switch.
   *
   * @return true or false when forced, or null to decide by environment
   */
  public Boolean enabled() {
    return enabled;
  }

  /**
   * The environments that get the tag when {@link #enabled()} is null.
   *
   * @return the environment names
   */
  public List<String> enabledEnvironments() {
    return enabledEnvironments;
  }

  /**
   * The environments the app runs in right now.
   *
   * @return the environment names
   */
  public List<String> environments() {
    return environments;
  }

  /**
   * Other attributes added to the tracker tag.
   *
   * @return attribute name to value
   */
  public Map<String, String> extraAttributes() {
    return extraAttributes;
  }

  /**
   * The Content Security Policy nonce.
   *
   * @return the nonce, or null
   */
  public String nonce() {
    return nonce;
  }

  /**
   * Page properties sent with every pageview and event. Never put anything that identifies a person
   * here.
   *
   * @return the properties as given (the rules in {@link PageProps} apply when printed)
   */
  public Map<String, Object> props() {
    return props;
  }

  /**
   * Whether the queue stub prints before the tracker.
   *
   * @return true to print it
   */
  public boolean queueStub() {
    return queueStub;
  }

  /**
   * Whether the tag should print in the current environment. {@link #enabled()} wins when set.
   * Otherwise the tag prints only when one of {@link #environments()} is in {@link
   * #enabledEnvironments()}.
   *
   * @return true when switched on
   */
  public boolean isEnabled() {
    if (enabled != null) {
      return enabled;
    }
    for (String environment : environments) {
      if (enabledEnvironments.contains(environment)) {
        return true;
      }
    }
    return false;
  }

  private static String resolve(String value, Function<String, String> lookup, String variable) {
    String clean = value == null ? "" : value.strip();
    if (clean.isEmpty() && lookup != null) {
      String fromEnv = lookup.apply(variable);
      clean = fromEnv == null ? "" : fromEnv.strip();
    }
    return clean.isEmpty() ? null : clean;
  }

  /** Null stays null. Anything else becomes a clean list: split on commas, trimmed, no blanks. */
  private static List<String> cleanList(Collection<String> values) {
    if (values == null) {
      return null;
    }
    List<String> clean = new ArrayList<>();
    for (String value : values) {
      if (value == null) {
        continue;
      }
      for (String part : value.split(",")) {
        String item = part.strip();
        if (!item.isEmpty()) {
          clean.add(item);
        }
      }
    }
    return List.copyOf(clean);
  }

  /** Builds {@link PrivatusSettings}. Every setting is optional except the site id. */
  public static final class Builder {
    private String siteId;
    private String scriptHost;
    private Collection<String> modules;
    private Collection<String> excludedPaths = List.of();
    private Collection<String> domains;
    private String spa;
    private boolean manual;
    private Boolean enabled;
    private Collection<String> enabledEnvironments = DEFAULT_ENABLED_ENVIRONMENTS;
    private Collection<String> environments = List.of();
    private Map<String, String> extraAttributes = new LinkedHashMap<>();
    private String nonce;
    private Map<String, Object> props = new LinkedHashMap<>();
    private boolean queueStub;
    private Function<String, String> variables = System::getenv;

    private Builder() {}

    /**
     * The site id. When blank, the {@code PRIVATUS_SITE_ID} environment variable is used.
     *
     * @param siteId the site id, for example {@code "pa_YOURSITEID"}
     * @return this builder
     */
    public Builder siteId(String siteId) {
      this.siteId = siteId;
      return this;
    }

    /**
     * Where {@code /js/pa.js} is served. When blank, {@code PRIVATUS_SCRIPT_HOST} is used, then
     * {@value PrivatusSettings#DEFAULT_SCRIPT_HOST}. A trailing slash is removed.
     *
     * @param scriptHost the origin, for example {@code "https://stats.example.com"}
     * @return this builder
     */
    public Builder scriptHost(String scriptHost) {
      this.scriptHost = scriptHost;
      return this;
    }

    /**
     * Tracker modules ({@code data-modules}). Null keeps the tracker default, an empty list loads
     * none.
     *
     * @param modules the modules, or null
     * @return this builder
     */
    public Builder modules(Collection<String> modules) {
      this.modules = modules;
      return this;
    }

    /**
     * Tracker modules ({@code data-modules}). No arguments loads none.
     *
     * @param modules the modules
     * @return this builder
     */
    public Builder modules(String... modules) {
      this.modules = Arrays.asList(modules);
      return this;
    }

    /**
     * Path globs the tracker never counts ({@code data-exclude}).
     *
     * @param excludedPaths the paths, for example {@code "/admin/*"}
     * @return this builder
     */
    public Builder excludedPaths(Collection<String> excludedPaths) {
      this.excludedPaths = excludedPaths;
      return this;
    }

    /**
     * Path globs the tracker never counts ({@code data-exclude}).
     *
     * @param excludedPaths the paths
     * @return this builder
     */
    public Builder excludedPaths(String... excludedPaths) {
      this.excludedPaths = Arrays.asList(excludedPaths);
      return this;
    }

    /**
     * Hostnames to count ({@code data-domains}). Null counts every host.
     *
     * @param domains the hostnames, or null
     * @return this builder
     */
    public Builder domains(Collection<String> domains) {
      this.domains = domains;
      return this;
    }

    /**
     * Hostnames to count ({@code data-domains}).
     *
     * @param domains the hostnames
     * @return this builder
     */
    public Builder domains(String... domains) {
      this.domains = Arrays.asList(domains);
      return this;
    }

    /**
     * The single-page app mode ({@code data-spa}): auto, history, hash or off. Only a mode other
     * than auto is printed. Unknown modes are ignored.
     *
     * @param spa the mode, or null
     * @return this builder
     */
    public Builder spa(String spa) {
      this.spa = spa;
      return this;
    }

    /**
     * True turns off the automatic pageview ({@code data-manual="true"}).
     *
     * @param manual true for manual mode
     * @return this builder
     */
    public Builder manual(boolean manual) {
      this.manual = manual;
      return this;
    }

    /**
     * True or false forces the tag on or off whatever the environment. Null decides by environment.
     *
     * @param enabled the switch, or null
     * @return this builder
     */
    public Builder enabled(Boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    /**
     * The environments that get the tag. The default is {@code production} only.
     *
     * @param enabledEnvironments environment names
     * @return this builder
     */
    public Builder enabledEnvironments(Collection<String> enabledEnvironments) {
      this.enabledEnvironments = enabledEnvironments;
      return this;
    }

    /**
     * The environment the app runs in right now, for example {@code "production"}.
     *
     * @param environment the environment name, or null
     * @return this builder
     */
    public Builder environment(String environment) {
      this.environments = environment == null ? List.of() : List.of(environment);
      return this;
    }

    /**
     * The environments the app runs in right now, for example the active Spring profiles.
     *
     * @param environments the environment names
     * @return this builder
     */
    public Builder environments(Collection<String> environments) {
      this.environments = environments;
      return this;
    }

    /**
     * Other attributes for the tracker tag, for example {@code data-mask}. Null values are left
     * out.
     *
     * @param extraAttributes attribute name to value
     * @return this builder
     */
    public Builder extraAttributes(Map<String, String> extraAttributes) {
      this.extraAttributes =
          extraAttributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(extraAttributes);
      return this;
    }

    /**
     * Adds one other attribute for the tracker tag.
     *
     * @param name the attribute name, for example {@code "data-debug"}
     * @param value the value
     * @return this builder
     */
    public Builder extraAttribute(String name, String value) {
      this.extraAttributes.put(name, value);
      return this;
    }

    /**
     * The Content Security Policy nonce for the script elements.
     *
     * @param nonce the nonce, or null
     * @return this builder
     */
    public Builder nonce(String nonce) {
      this.nonce = nonce;
      return this;
    }

    /**
     * Page properties sent with every pageview and event, replacing any set before. Never put
     * names, emails, user ids or anything else that identifies a person here.
     *
     * @param props property name to value
     * @return this builder
     */
    public Builder props(Map<String, ?> props) {
      this.props = props == null ? new LinkedHashMap<>() : new LinkedHashMap<>(props);
      return this;
    }

    /**
     * Adds page properties on top of the ones already set.
     *
     * @param props property name to value
     * @return this builder
     */
    public Builder addProps(Map<String, ?> props) {
      if (props != null) {
        this.props.putAll(props);
      }
      return this;
    }

    /**
     * True prints the queue stub before the tracker, so {@code privatus.track(...)} works before
     * the tracker loads.
     *
     * @param queueStub true to print it
     * @return this builder
     */
    public Builder queueStub(boolean queueStub) {
      this.queueStub = queueStub;
      return this;
    }

    /**
     * Where environment variables are read from. The default is {@link System#getenv(String)}.
     * Frameworks pass their own lookup here, and tests pass a map.
     *
     * @param variables variable name to value, or null to read none
     * @return this builder
     */
    public Builder variables(Function<String, String> variables) {
      this.variables = variables;
      return this;
    }

    /**
     * Applies overrides from a map, for template engines. Keys may be written as {@code
     * excludedPaths}, {@code excluded-paths} or {@code excluded_paths}. Known keys: siteId,
     * scriptHost, modules, excludedPaths, domains, spa, manual, enabled, environment,
     * extraAttributes, nonce, props (added on top), queueStub.
     *
     * @param overrides setting name to value, may be null
     * @return this builder
     * @throws IllegalArgumentException for an unknown key
     */
    public Builder override(Map<?, ?> overrides) {
      if (overrides == null) {
        return this;
      }
      for (Map.Entry<?, ?> entry : overrides.entrySet()) {
        String key = String.valueOf(entry.getKey()).replace("-", "").replace("_", "");
        Object value = entry.getValue();
        switch (key.toLowerCase(Locale.ROOT)) {
          case "siteid" -> siteId(text(value));
          case "scripthost" -> scriptHost(text(value));
          case "modules" -> modules(list(value));
          case "excludedpaths", "exclude" -> excludedPaths(list(value));
          case "domains" -> domains(list(value));
          case "spa" -> spa(text(value));
          case "manual" -> manual(bool(value));
          case "enabled" -> enabled(value == null ? null : bool(value));
          case "environment", "environments" -> environments(list(value));
          case "extraattributes" -> extraAttributes.putAll(stringMap(value));
          case "nonce" -> nonce(text(value));
          case "props" -> addProps(objectMap(value));
          case "queuestub" -> queueStub(bool(value));
          default ->
              throw new IllegalArgumentException("Unknown Privatus Analytics setting: " + key);
        }
      }
      return this;
    }

    /**
     * Builds the settings.
     *
     * @return the settings
     */
    public PrivatusSettings build() {
      return new PrivatusSettings(this);
    }

    private static String text(Object value) {
      return value == null ? null : value.toString();
    }

    private static boolean bool(Object value) {
      return value instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(value));
    }

    private static List<String> list(Object value) {
      if (value == null) {
        return null;
      }
      if (Values.isList(value)) {
        List<String> items = new ArrayList<>();
        for (Object item : Values.items(value)) {
          items.add(item.toString());
        }
        return items;
      }
      return List.of(value.toString());
    }

    private static Map<String, String> stringMap(Object value) {
      Map<String, String> map = new LinkedHashMap<>();
      if (value instanceof Map<?, ?> source) {
        source.forEach((k, v) -> map.put(String.valueOf(k), v == null ? null : v.toString()));
      } else if (value != null) {
        throw new IllegalArgumentException("extraAttributes must be a map");
      }
      return map;
    }

    private static Map<String, Object> objectMap(Object value) {
      Map<String, Object> map = new LinkedHashMap<>();
      if (value instanceof Map<?, ?> source) {
        source.forEach((k, v) -> map.put(String.valueOf(k), v));
      } else if (value != null) {
        throw new IllegalArgumentException("props must be a map");
      }
      return map;
    }
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PrivatusSettings that)) {
      return false;
    }
    return manual == that.manual
        && queueStub == that.queueStub
        && Objects.equals(siteId, that.siteId)
        && scriptHost.equals(that.scriptHost)
        && Objects.equals(modules, that.modules)
        && excludedPaths.equals(that.excludedPaths)
        && Objects.equals(domains, that.domains)
        && Objects.equals(spa, that.spa)
        && Objects.equals(enabled, that.enabled)
        && enabledEnvironments.equals(that.enabledEnvironments)
        && environments.equals(that.environments)
        && extraAttributes.equals(that.extraAttributes)
        && Objects.equals(nonce, that.nonce)
        && props.equals(that.props);
  }

  @Override
  public int hashCode() {
    return Objects.hash(siteId, scriptHost, modules, excludedPaths, domains, spa, enabled, nonce);
  }

  @Override
  public String toString() {
    return "PrivatusSettings{siteId="
        + siteId
        + ", scriptHost="
        + scriptHost
        + ", enabled="
        + isEnabled()
        + "}";
  }
}
