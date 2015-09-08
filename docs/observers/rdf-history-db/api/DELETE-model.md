---
currentMenu: historydb-api
parentMenu: historydb
parent2Menu: observers
---

[&#9664; Back to API list](.)

# `DELETE` /resources/:id

## Description

Registers the intention of deleting a model with the specified id.

## URL Parameters

Name | Type | Description
--- | --- | ---
``:id`` | string | the id of the model that will be deleted

## Data Parameters

None.

## Response

```
Status: 204 No Content
```

```
-
```

## Errors

* **400 Id not valid** -- The id is malformed, or it wasn't given at all.
* **500 Server error** -- Given when it is impossible to communicate either with the queue or the datastore.

The errors aren't implemented at the time of writing this, so every request will always answer with a 204 status even if there is a problem.

***

## Example

### Request

```
DELETE /resources/vm1
```

```
-
```

### Response

```
Status: 204 No Content
```

```
-
```
