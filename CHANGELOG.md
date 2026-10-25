# Changelog

## 1.0.0 - 2026-10-25

- Initial release.
- `privatus-analytics-core`: `Tag`, `EventAttributes`, `ClickAttribute`, `PageProps` and
  `QueueStub`, with no dependencies.
- `privatus-analytics-spring-boot-starter`: settings under `privatus.analytics`, a Thymeleaf
  dialect (`privatus:analytics`, `privatus:event`, `privatus:props`, `privatus:click`), a
  `privatusAnalytics` request and model attribute for JSP and FreeMarker, and an optional filter
  that adds the tag to the head of HTML responses (off by default).
- Page properties from settings or from a `PrivatusPagePropsProvider` bean per request.
- Skips every profile except `prod` and `production` by default.
