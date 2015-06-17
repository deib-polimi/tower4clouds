---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `GET` /metrics/:id/observers

## Description
Returns the list of observers attached to the metric.

***

## URL Parameters

None

***

## Response

**Status:** **200 OK**

**Body:** A json array with information about attached observers.

***

## Errors

* **404 Resource not found** - The metric does not exist: ...

***

## Example
**Request**

	GET v1/metrics/ResponseTime/observers

**Response**

	Status: 200 OK

``` json
[
	{
	"id": "109384935893",
	"format": "GRAPHITE",
	"protocol": "TCP",
	"observerHost": "123.123.123.123",
	"observerPort": 2003
	},
	{
	"id": "109384935894",
	"format": "TOWER/JSON",
	"protocol": "HTTP",
	"callbackUrl": "http://123.123.123.123:8001/data"
	}
]
```