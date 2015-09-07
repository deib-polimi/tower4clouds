---
currentMenu: historydb-api-post-resources
parentMenu: historydb-api
parent2Menu: historydb
parent3Menu: observers
---

# Models

```
POST /resources
```

## Description

Records the new resource as an update to an existing model (that if it doesn't exist will be instead created).

## URL Parameters

None.

## Data Parameters

A JSON containing all the resources that must be added to the knowledge base.

## Response

```
Status: 204 No Content
```

```
-
```

## Errors

* **400 Resources not valid** -- The resources aren't valid or aren't expressed in the expected way.
* **500 Server error** -- Given when it is impossible to communicate either with the queue or the datastore.

The errors aren't implemented at the time of writing this, so every request will always answer with a 204 status even if there is a problem.

## Example

### Request

```
POST /resources
```

```json
{
  "internalComponents": [
    {
      "id": "mic3", 
      "providedMethods": [
        "mic3-register", 
        "mic3-answerQuestions", 
        "mic3-saveAnswers"
      ], 
      "requiredComponents": [
        "frontend3"
      ], 
      "type": "Mic"
    }
  ], 
  "methods": [
    {
      "id": "mic3-answerQuestions", 
      "type": "answerQuestions"
    }, 
    {
      "id": "mic3-saveAnswers", 
      "type": "saveAnswers"
    }, 
    {
      "id": "mic3-register", 
      "type": "register"
    }
  ], 
  "vMs": [
    {
      "cloudProvider": "amazon", 
      "id": "frontend3", 
      "numberOfCPUs": 2, 
      "type": "Frontend"
    }
  ]
}
```

### Response

```
Status: 204 No Content
```

```
-
```
