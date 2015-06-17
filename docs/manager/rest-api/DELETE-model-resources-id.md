---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `DELETE` /model/resources/:id

## Description
Delete the specified resource from the knowledge base if it exists

***

## URL Parameters

None.

***

## Response

**Status:** **204 No Content**

***

## Errors

* **404 Resource not found** - The resource does not exist (not implemented yet), answer is 204 even if it doesn't exist

***

## Example
**Request**

	DELETE v1/model/resources/vm1
