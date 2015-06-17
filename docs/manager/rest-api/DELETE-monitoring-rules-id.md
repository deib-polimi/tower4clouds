---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `DELETE` /monitoring-rules/:id

## Description
Deletes a monitoring rule.

***

## URL Parameters

None.

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

	DELETE v1/monitoring-rules/CPURule