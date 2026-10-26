# Privatus Analytics for Java

**Real visitor numbers for your Java web app. No cookies, no cookie pop-up, no bots.**

You want to know if people use your Java web app. But Google Analytics uses
cookies, so in many countries you must show a cookie pop-up first (the box that
asks visitors to say yes to cookies). Many people click no, so they never show
up in your numbers. Bots add fake visits on top.

The Spring Boot starter adds the Privatus Analytics script to your pages with a
Thymeleaf dialect, a helper for JSP and FreeMarker, or a filter that puts it in
the page head for you. The script sets no cookies and keeps no IP addresses.
Bots are filtered out before they are counted.

So you see how many real people came, which pages they looked at and where they
came from, on one simple dashboard. A report comes to your inbox each week, and
you can set alerts for when traffic jumps or drops. And you don't need a cookie
pop-up for your analytics.

**Free plan, no card:** 10,000 events a month (an event is a page view or an action you track) on up to 3 sites. [Start free at privatusanalytics.com](https://privatusanalytics.com).

- **No cookies.** No cookies and no hidden IDs on your visitors' devices, and no IP
  addresses are kept. Laws differ by country, so check your own legal advice.
- **Real people, not bots.** Known bots, data centers and spam visits are left
  out on every plan.
- **Small and fast.** The script is about 3 KB when compressed.

## What this package does

- The Spring Boot starter gives you a Thymeleaf dialect, a helper for JSP and
  FreeMarker, and an optional filter that puts the Privatus Analytics script in
  the `<head>` for you.
- Only prints the browser script and some HTML attributes. It never sends events
  from your server and sets no cookies.
- Never reads IP addresses or browser details.

You need a Privatus Analytics account to see reports, and there is a free plan.
Sign up at https://privatusanalytics.com.

There are two packages:

* `privatus-analytics-spring-boot-starter`: for Spring Boot 3 apps. Most people
  want this one.
* `privatus-analytics-core`: the plain Java part, with no dependencies. Use it
  with any other framework.

## Requirements

* Java 17 or later.
* Spring Boot 3.2 to 3.5, with Spring MVC (for the starter).
* A Privatus Analytics site id (it starts with `pa_`). You find it under
  Site settings, Tracking.

## Install

Maven:

```xml
<dependency>
  <groupId>com.privatusanalytics</groupId>
  <artifactId>privatus-analytics-spring-boot-starter</artifactId>
  <version>1.0.1</version>
</dependency>
```

Gradle:

```groovy
implementation "com.privatusanalytics:privatus-analytics-spring-boot-starter:1.0.1"
```

For the full setup guide and every tracker option, see the [Privatus Analytics
docs](https://privatusanalytics.com/docs).

Set your site id with an environment variable:

```sh
PRIVATUS_SITE_ID=pa_YOURSITEID
```

or in `application.properties`:

```properties
privatus.analytics.site-id=pa_YOURSITEID
```

## Add the tag

### Thymeleaf

Put the tag in the `<head>` of your layout:

```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:privatus="https://privatusanalytics.com/thymeleaf">
<head>
  <title>My app</title>
  <th:block privatus:analytics/>
</head>
```

With the `prod` or `production` profile active, it prints:

```html
<script defer src="https://privatusstats.com/js/pa.js" data-site="pa_YOURSITEID"></script>
```

The `xmlns:privatus` line is optional. It only stops your editor from
complaining, and it is removed from the page.

### JSP and FreeMarker

Every request and model gets a `privatusAnalytics` helper:

```jsp
<head>
  <title>My app</title>
  ${privatusAnalytics.tag}
</head>
```

```ftl
<head>
  <title>My app</title>
  ${privatusAnalytics.tag?no_esc}
</head>
```

FreeMarker escapes HTML for you, so `?no_esc` tells it the tag is already safe.

### Any other Java code

With only `privatus-analytics-core`, build the settings once and print the tag:

```java
PrivatusSettings settings = PrivatusSettings.builder()
    .siteId("pa_YOURSITEID")               // or the PRIVATUS_SITE_ID environment variable
    .environment(System.getenv("APP_ENV")) // the tag prints only for "production"
    .build();

String html = Tag.render(settings);
```

The tag prints nothing when there is no site id, and nothing outside
production (see below).

If your site uses a Content Security Policy nonce (a random code that tells
the browser which inline scripts to trust), put it in the request attribute
`cspNonce` and the starter adds it to every script element. Use
`privatus.analytics.nonce-attribute` to read another attribute name. With
`privatus-analytics-core`, call `.nonce("...")`.

### Or let the starter add it

If you would rather not touch your layouts, switch on the filter:

```properties
privatus.analytics.auto-inject=true
```

It adds the tag just before `</head>` in uncompressed `text/html` responses
with status 200. It skips Ajax (background) requests, pages without a
`<head>`, and pages that already have the tag. Responses that have a
`Content-Encoding` header are left alone. Compression set with
`server.compression.enabled` happens after the filter, so it works with it.
Other responses stream through untouched.

## Configuration

Every setting is optional except the site id.

```properties
# application-prod.properties
privatus.analytics.site-id=pa_YOURSITEID
privatus.analytics.script-host=https://privatusstats.com
privatus.analytics.modules=engage,auto,vitals,clicks,ask
privatus.analytics.excluded-paths=/admin/*,/preview/*
privatus.analytics.domains=example.com,www.example.com
privatus.analytics.spa=auto
privatus.analytics.manual=false
privatus.analytics.auto-inject=false
privatus.analytics.enabled-profiles=prod,production
privatus.analytics.extra-attributes.data-mask=/orders/*->/orders/:id
privatus.analytics.props.app_version=4.2
privatus.analytics.queue-stub=false
```

| Setting | Default | What it does |
|---|---|---|
| `site-id` | `PRIVATUS_SITE_ID` | The `data-site` attribute. Nothing prints without it |
| `script-host` | `https://privatusstats.com` | Where `/js/pa.js` is served (or `PRIVATUS_SCRIPT_HOST`). Change it only if you host your own copy |
| `modules` | tracker default (`engage`) | The `data-modules` attribute. Setting it replaces the default, so keep `engage` for time on page and scroll depth. An empty value (`modules=`) loads none |
| `excluded-paths` | none | The `data-exclude` attribute. Path patterns the tracker never counts |
| `domains` | all hosts | The `data-domains` attribute. Count only these hostnames |
| `spa` | `auto` | The `data-spa` attribute, for single-page apps: `auto`, `history`, `hash` or `off` |
| `manual` | `false` | The `data-manual` attribute. `true` turns off the automatic pageview |
| `enabled-profiles` | `prod,production` | Spring profiles that get the tag |
| `enabled` | not set | `true` or `false` forces the tag on or off in every profile |
| `auto-inject` | `false` | Adds the tag with a servlet filter |
| `extra-attributes` | none | Any other `data-*` attribute |
| `props` | none | Page properties for every page (see below) |
| `queue-stub` | `false` | Prints the queue stub before the tag (see below). It still prints when the tag is off, so `privatus.track` calls never fail |
| `nonce-attribute` | `cspNonce` | The request attribute that holds your nonce |
| `expose-model-attribute` | `true` | Adds the `privatusAnalytics` helper to requests and models |

Each tracker attribute is explained in the [script attributes
reference](https://privatusanalytics.com/docs/tracker/attributes), and the
modules in [Modules](https://privatusanalytics.com/docs/tracker/modules).

You can also change a setting for one page:

```html
<th:block privatus:analytics="${ {modules: {'engage','auto'}, excludedPaths: {'/preview/*'}} }"/>
```

```jsp
${privatusAnalytics.tagWith({'modules': ['engage', 'auto']})}
```

```ftl
${privatusAnalytics.tagWith({"modules": ["engage", "auto"]})?no_esc}
```

```java
Tag.render(settings.toBuilder().modules("engage", "auto").build());
```

### Development and test

Only the `prod` and `production` profiles get the tag by default, so your own
clicking around in development and tests never reaches your reports. To see
the tag while you build, set `privatus.analytics.enabled=true`. The tracker
also ignores `localhost` unless you pass `data-allow-local` or `data-debug`
(see [Debug mode](https://privatusanalytics.com/docs/tracker/debug)):

```properties
privatus.analytics.extra-attributes.data-debug=true
```

### Leaving people out

Use `excluded-paths` for admin areas. To leave out signed-in staff, print the
tag only for other people:

```html
<th:block th:unless="${currentUser?.admin}" privatus:analytics/>
```

## Product analytics: see which features people use

Privatus Analytics can show which buttons people press, which forms they send, and how
that differs by plan. These helpers print plain HTML, so they print in every
profile. That keeps your development and production pages the same. The
tracker only sends anything where the tag is on.

### Name an event on a button, link or form

```html
<button privatus:event="Export CSV" privatus:props="${ {format:'csv', source:'toolbar'} }">
  Export
</button>
```

prints

```html
<button data-privatus-event="Export CSV" data-privatus-prop-format="csv" data-privatus-prop-source="toolbar">
  Export
</button>
```

On a `<form>`, the event is sent when the form is submitted:

```html
<form method="post" action="/signup" privatus:event="Signup" privatus:props="${ {plan:'pro'} }">
```

In JSP and FreeMarker:

```jsp
<button ${privatusAnalytics.eventAttrs('Export CSV', {'format': 'csv'})}>Export</button>
```

```ftl
<button ${privatusAnalytics.eventAttrs("Export CSV", {"format": "csv"})?no_esc}>Export</button>
```

In plain Java: `PrivatusAnalytics.eventAttrs("Export CSV", Map.of("format", "csv"))`.

The rules: the event name is trimmed and cut to 120 characters. An empty name
prints nothing. Property names are lowercased, and any character other than
`a-z`, `0-9` and `_` becomes `_` (cut to 64 characters). Up to 30 properties
are kept. Values become text: `true` and `false`, numbers as written, lists
joined with commas. Empty (null) values are left out, and values are cut to
500 characters. Everything is escaped, so it is safe to print.

### Give an element a steady name in click maps

```html
<a href="/signup" privatus:click="pricing.cta">Start free</a>
```

prints `data-privatus-click="pricing.cta"`. Use 1 to 100 letters, digits,
`_`, `.`, `:` or `-`. Any other name prints nothing (and a warning in the log
while the tag is off). In JSP: `${privatusAnalytics.clickAttr('pricing.cta')}`.
In plain Java: `PrivatusAnalytics.clickAttr("pricing.cta")`.

### Page properties: plan, role, version

Page properties are sent with every pageview and event on the page, so you can
split any report by them. Good ones are plan, role, app version, feature flags
and account size.

**Never put names, emails, user ids or anything else that points to one person
in page properties.** Privatus Analytics counts visitors without any id. Put groups
(plan, role), not people. Emails and long ids are also removed by the server.

Set the same properties for every page in your settings:

```properties
privatus.analytics.props.app_version=4.2
```

Set them per request with a bean:

```java
@Bean
PrivatusPagePropsProvider pageProps(AccountService accounts) {
  return request -> {
    Account account = accounts.current(request);
    return account == null ? null : Map.of("plan", account.plan(), "role", account.role());
  };
}
```

Or for one page: `<th:block privatus:analytics="${ {props: {section:'docs'}} }"/>`.
These are added on top of each other, so the later ones win.

The tag then prints a small script before the tracker:

```html
<script>window.privatusPageProps=function(){return {"plan":"pro","role":"admin"}}</script>
<script defer src="https://privatusstats.com/js/pa.js" data-site="pa_YOURSITEID" data-props="privatusPageProps"></script>
```

Names follow the same rules as event properties. Numbers and `true`/`false`
stay as they are, and lists stay lists (up to 30 items).

### Track from your own JavaScript

```js
privatus.track("Feature used", { feature: "export" });
```

If your code can run before the tracker loads, switch on the queue stub. It
keeps early calls and sends them once the tracker is ready:

```properties
privatus.analytics.queue-stub=true
```

It prints this before the tag (with your nonce if you have one):

```html
<script>window.privatus=window.privatus||{q:[]};['track','pageview','props','optOut','optIn','ready'].forEach(function(m){window.privatus[m]=window.privatus[m]||function(){window.privatus.q.push([m,Array.prototype.slice.call(arguments)])}})</script>
```

The stub prints even where the tag is off (outside the `prod` profile, with
`enabled=false`, or with no site id). Then only the stub prints: no tracker
and no page properties. So you can always call `privatus.track(...)`. In
development the calls just wait in the queue and are never sent. The filter
adds nothing when the tag is off. See the [JavaScript API
docs](https://privatusanalytics.com/docs/tracker/javascript-api).

### Naming events

Use "Thing done" names, like `Report exported`, `Invite sent` or
`Plan upgraded`. Keep the name short and the same everywhere, and put the
details in properties: `Report exported` with `format: csv`, not
`Report exported as CSV`. This keeps your event list short and easy to read.

Then build on your events:

* [Funnels](https://privatusanalytics.com/docs/features/funnels) show where
  people drop out between steps.
* [Click maps](https://privatusanalytics.com/docs/features/click-maps) show
  what people click on a page.
* [Goals](https://privatusanalytics.com/docs/features/goals) turn an event
  into a conversion you can follow.
* [Events](https://privatusanalytics.com/docs/events) explains every event
  option.

## Privacy

The script sets no cookies and stores nothing in the browser. This library
sends nothing from your server. See the [privacy
documentation](https://privatusanalytics.com/docs/privacy) for what is
collected.

If you have a privacy policy, it should say which tools handle your visitors' data. You can paste this:

> This website uses [Privatus Analytics](https://privatusanalytics.com/privacy-analytics) to count visits. It
> does not use cookies and does not follow you from one website to another. Your IP address is used for a moment
> to count visits and is never stored.

Change it to fit your site. The [privacy policy generator](https://privatusanalytics.com/tools/privacy-policy-generator)
makes a longer version with your site's name in it.

## Development

The tests run in Docker so your local Java does not matter:

```sh
./run-docker.sh
```

It runs the JUnit tests (with a small Spring Boot test app for the starter),
checks the code format with Spotless (google-java-format), and builds the
jars, sources jars and javadoc jars. To fix the format:

```sh
docker run --rm -v "$PWD":/app -v privatus-java-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-21 mvn -B spotless:apply
```

## Contributing

Bug reports and pull requests are welcome at
https://github.com/Privatus-Analytics/java.

## License

MIT. See [LICENSE](LICENSE). Privatus Analytics is a product of Two Phase LLC,
Wyoming, USA.
