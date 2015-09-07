---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `DELETE` /metrics/:id/observers/:id

## Description
Detach the observer from the metric.

***

## URL Parameters

None.

***

## Response

**Status:** **204 No Content**

***

## Errors

* **404 Resource not found** - Either the metric or the observer was not found: ...

***

## Example
**Request**

	DELETE v1/metrics/ResponseTime/observers/109384935893
