---
currentMenu: historydb-api-put-resources
parentMenu: historydb-api
parent2Menu: historydb
parent3Menu: observers
---

# Models

```
PUT /resources
```

## Description

Records the new resource as a new model.

## URL Parameters

None.

## Data Parameters

A JSON containing the new model that must be uploaded on the knowledge base.

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
PUT /resources
```

```json
{
  "cloudProviders": [
    {
      "id": "amazon", 
      "type": "IaaS"
    }
  ], 
  "internalComponents": [
    {
      "id": "mic1", 
      "providedMethods": [
        "mic1-register", 
        "mic1-answerQuestions", 
        "mic1-saveAnswers"
      ], 
      "requiredComponents": [
        "frontend1"
      ], 
      "type": "Mic"
    },
    {
      "id": "mic2", 
      "providedMethods": [
        "mic2-register", 
        "mic2-answerQuestions", 
        "mic2-saveAnswers"
      ], 
      "requiredComponents": [
        "frontend2"
      ], 
      "type": "Mic"
    }
  ], 
  "methods": [
    {
      "id": "mic1-answerQuestions", 
      "type": "answerQuestions"
    }, 
    {
      "id": "mic1-saveAnswers", 
      "type": "saveAnswers"
    }, 
    {
      "id": "mic1-register", 
      "type": "register"
    },
    {
      "id": "mic2-answerQuestions", 
      "type": "answerQuestions"
    }, 
    {
      "id": "mic2-saveAnswers", 
      "type": "saveAnswers"
    }, 
    {
      "id": "mic2-register", 
      "type": "register"
    }
  ], 
  "vMs": [
    {
      "cloudProvider": "amazon", 
      "id": "frontend1", 
      "numberOfCPUs": 2, 
      "type": "Frontend"
    },
    {
      "cloudProvider": "amazon", 
      "id": "frontend2", 
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
