---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `GET` /metrics

## Description
Returns the list of [Observable Metrics].

***

## URL Parameters

None

***

## Response

**Status:** **200 OK**

**Body:** A json array with the observable metrics.

***

## Errors

None

***

## Example
**Request**

	GET v1/metrics

**Response**

	Status: 200 OK

``` json
["CpuUtilization", "ResponseTime", "CpuUtilizationViolation"]
```

[Observable Metrics]: ../../monitoring-rules/actions.html#output-metric