---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `GET` /monitoring-rules/:id

## Description
Updates monitoring rules properties.

***

## URL Parameters

* `enabled`
	* type: `boolean`
	* optional: no
	* description: enable/disable a rule

***

## Response

**Status:** **204 No Content**

***

## Errors

All known errors cause the resource to return HTTP error code header together with a description of the error.

* **404 Resource not found** - Rule does not exist: ...

***

## Example

**Request**

	GET v1/monitoring-rules/CPURule?enabled=false