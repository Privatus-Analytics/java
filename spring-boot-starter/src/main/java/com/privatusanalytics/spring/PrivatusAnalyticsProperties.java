package com.privatusanalytics.spring;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under {@code privatus.analytics}. Every setting is optional except the site id.
 *
 * <pre>
 * privatus.analytics.site-id=pa_YOURSITEID
 * privatus.analytics.modules=engage,auto
 * </pre>
 */
@ConfigurationProperties(prefix = "privatus.analytics")
public class PrivatusAnalyticsProperties {
  /** The site id (starts with pa_). Falls back to the PRIVATUS_SITE_ID environment variable. */
  private String siteId;

  /**
   * Origin that serves /js/pa.js. Falls back to PRIVATUS_SCRIPT_HOST, then
   * https://privatusstats.com.
   */
  private String scriptHost;

  /** Tracker modules to load, for example engage,auto. Unset keeps the tracker default. */
  private List<String> modules;

  /** Path globs the tracker never counts, for example /admin/*,/preview/*. */
  private List<String> excludedPaths = new ArrayList<>();

  /** Hostnames to count. Unset counts every host. */
  private List<String> domains;

  /** One of auto, history, hash, off. Unset keeps the tracker default. */
  private String spa;

  /** True turns off the automatic pageview. */
  private boolean manual = false;

  /** True or false forces the tag on or off whatever the profile. Unset uses enabled-profiles. */
  private Boolean enabled;

  /** Spring profiles that get the tag. Used when enabled is unset. */
  private List<String> enabledProfiles = new ArrayList<>(List.of("prod", "production"));

  /** True adds the tag to the head of HTML responses with a servlet filter. */
  private boolean autoInject = false;

  /** Extra data- attributes, for example data-mask. */
  private Map<String, String> extraAttributes = new LinkedHashMap<>();

  /**
   * Page properties sent with every pageview and event, for example plan or role. Never put
   * anything that identifies a person here.
   */
  private Map<String, Object> props = new LinkedHashMap<>();

  /** True prints the queue stub before the tracker, so privatus.track works before it loads. */
  private boolean queueStub = false;

  /** Request attribute that holds your Content Security Policy nonce, if you use one. */
  private String nonceAttribute = "cspNonce";

  /** True adds the privatusAnalytics helper to every request and model (for JSP and FreeMarker). */
  private boolean exposeModelAttribute = true;

  /**
   * The site id.
   *
   * @return the site id
   */
  public String getSiteId() {
    return siteId;
  }

  /**
   * Sets the site id.
   *
   * @param siteId the site id
   */
  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  /**
   * The script host.
   *
   * @return the script host
   */
  public String getScriptHost() {
    return scriptHost;
  }

  /**
   * Sets the script host.
   *
   * @param scriptHost the script host
   */
  public void setScriptHost(String scriptHost) {
    this.scriptHost = scriptHost;
  }

  /**
   * The tracker modules.
   *
   * @return the modules, or null
   */
  public List<String> getModules() {
    return modules;
  }

  /**
   * Sets the tracker modules.
   *
   * @param modules the modules
   */
  public void setModules(List<String> modules) {
    this.modules = modules;
  }

  /**
   * The excluded paths.
   *
   * @return the paths
   */
  public List<String> getExcludedPaths() {
    return excludedPaths;
  }

  /**
   * Sets the excluded paths.
   *
   * @param excludedPaths the paths
   */
  public void setExcludedPaths(List<String> excludedPaths) {
    this.excludedPaths = excludedPaths;
  }

  /**
   * The domains to count.
   *
   * @return the domains, or null
   */
  public List<String> getDomains() {
    return domains;
  }

  /**
   * Sets the domains to count.
   *
   * @param domains the domains
   */
  public void setDomains(List<String> domains) {
    this.domains = domains;
  }

  /**
   * The single-page app mode.
   *
   * @return the mode
   */
  public String getSpa() {
    return spa;
  }

  /**
   * Sets the single-page app mode.
   *
   * @param spa the mode
   */
  public void setSpa(String spa) {
    this.spa = spa;
  }

  /**
   * Whether manual mode is on.
   *
   * @return true for manual mode
   */
  public boolean isManual() {
    return manual;
  }

  /**
   * Sets manual mode.
   *
   * @param manual true for manual mode
   */
  public void setManual(boolean manual) {
    this.manual = manual;
  }

  /**
   * The forced switch.
   *
   * @return true, false or null
   */
  public Boolean getEnabled() {
    return enabled;
  }

  /**
   * Sets the forced switch.
   *
   * @param enabled true, false or null
   */
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * The profiles that get the tag.
   *
   * @return the profiles
   */
  public List<String> getEnabledProfiles() {
    return enabledProfiles;
  }

  /**
   * Sets the profiles that get the tag.
   *
   * @param enabledProfiles the profiles
   */
  public void setEnabledProfiles(List<String> enabledProfiles) {
    this.enabledProfiles = enabledProfiles;
  }

  /**
   * Whether the filter adds the tag for you.
   *
   * @return true when on
   */
  public boolean isAutoInject() {
    return autoInject;
  }

  /**
   * Switches the filter on or off.
   *
   * @param autoInject true to switch it on
   */
  public void setAutoInject(boolean autoInject) {
    this.autoInject = autoInject;
  }

  /**
   * The extra attributes.
   *
   * @return attribute name to value
   */
  public Map<String, String> getExtraAttributes() {
    return extraAttributes;
  }

  /**
   * Sets the extra attributes.
   *
   * @param extraAttributes attribute name to value
   */
  public void setExtraAttributes(Map<String, String> extraAttributes) {
    this.extraAttributes = extraAttributes;
  }

  /**
   * The page properties.
   *
   * @return property name to value
   */
  public Map<String, Object> getProps() {
    return props;
  }

  /**
   * Sets the page properties.
   *
   * @param props property name to value
   */
  public void setProps(Map<String, Object> props) {
    this.props = props;
  }

  /**
   * Whether the queue stub prints.
   *
   * @return true when on
   */
  public boolean isQueueStub() {
    return queueStub;
  }

  /**
   * Switches the queue stub on or off.
   *
   * @param queueStub true to print it
   */
  public void setQueueStub(boolean queueStub) {
    this.queueStub = queueStub;
  }

  /**
   * The request attribute that holds the nonce.
   *
   * @return the attribute name
   */
  public String getNonceAttribute() {
    return nonceAttribute;
  }

  /**
   * Sets the request attribute that holds the nonce.
   *
   * @param nonceAttribute the attribute name
   */
  public void setNonceAttribute(String nonceAttribute) {
    this.nonceAttribute = nonceAttribute;
  }

  /**
   * Whether the helper is added to requests and models.
   *
   * @return true when on
   */
  public boolean isExposeModelAttribute() {
    return exposeModelAttribute;
  }

  /**
   * Switches the model attribute on or off.
   *
   * @param exposeModelAttribute true to add it
   */
  public void setExposeModelAttribute(boolean exposeModelAttribute) {
    this.exposeModelAttribute = exposeModelAttribute;
  }
}
