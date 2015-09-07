---
currentMenu: rest-api
parentMenu: manager
---

# REST API

Version 1

The REST endpoing is accessible at the [configured][Configuration] manager port under the url path /v1.

### Monitoring Rules

- **[`GET` /monitoring-rules](GET-monitoring-rules.md)**
- **[`GET` /monitoring-rules/:id](GET-monitoring-rules-id.md)**
- **[`POST` /monitoring-rules](POST-monitoring-rules.md)**
- **[`DELETE` /monitoring-rules/:id](DELETE-monitoring-rules-id.md)**

### Metrics

- **[`GET` /metrics](GET-metrics.md)**
- **[`GET` /metrics/:id/observers](GET-metrics-id-observers.md)**
- **[`POST` /metrics/:id/observers](POST-metrics-id-observers.md)**
- **[`DELETE` /metrics/:id/observers/:id](DELETE-metrics-id-observers-id.md)**

[To be completed]

[Configuration]: ../configuration.md
