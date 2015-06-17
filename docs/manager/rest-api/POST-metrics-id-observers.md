---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `POST` /metrics/:id/observers

## Description
Attach an observer to the metric.

***

## URL Parameters

None

***

## Data Parameters

A JSON object containing the following fields:
* `format`
	* type: `String`
	* optional: yes
	* default: `RDF/JSON`
	* description: specifies the [serialization format]
* `protocol`
	* type: `String`
	* optional: yes
	* default: `HTTP`
	* description: specifies the [transfer protocol]
* `callbackUrl`
	* type: `String`
	* optional: no, if protocol is `HTTP`
	* description: the full endpoint url of the observer
* `observerHost`
	* type: `String`
	* optional: no, if protocol is either `TCP` or `UDP`
	* description: the IP address of the observer
* `observerPort`
	* type: `int`
	* optional: no, if protocol is either `TCP` or `UDP`
	* description: the port to which the observer is listening to

***

## Response

**Status:** **201 Created**

**Body:** a json object containing the information about the observer just registered together with its server assigned id.

***

## Errors

* **400 Bad Request** - <description of the error within the provided json>
* **404 Resource not found** - The metric does not exist.

***

## Example
**Request**

	POST v1/metrics/ResponseTime/observers
	
```json
{
	"format": "GRAPHITE",
	"protocol": "TCP",
	"observerHost": "123.123.123.123",
	"observerPort": 2003
}
```

**Response**

	Status: 201 Created

```json
{
	"id": "109384935893",
	"format": "GRAPHITE",
	"protocol": "TCP",
	"observerHost": "123.123.123.123",
	"observerPort": 2003
}
```

[serialization format]: ../../data-analyzer/serialization-formats.html
[transfer protocol]: ../../data-analyzer/transfer-protocol.html