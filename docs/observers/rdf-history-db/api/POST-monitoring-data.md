---
currentMenu: historydb-api-post-monitoring-data
parentMenu: historydb-api
parent2Menu: historydb
parent3Menu: observers
---

# Results

```
POST /monitoringdata
```

## Description

Send a monitoring datum to the metrics observer.

## URL Parameters

None.

## Data Parameters

A monitoring datum in [json rdf serialization format](https://dvcs.w3.org/hg/rdf/raw-file/default/rdf-json/index.html) of the following rdf model (here in ttl format for simplicity):

```
[] <http://www.modaclouds.eu/rdfs/1.0/monitoringdata#metric> ?metric;
<http://www.modaclouds.eu/rdfs/1.0/monitoringdata#timestamp> ?timestamp;
<http://www.modaclouds.eu/rdfs/1.0/monitoringdata#value> ?value;
<http://www.modaclouds.eu/rdfs/1.0/monitoringdata#resourceId> ?resourceId .
```

## Response

```
Status: 204 No Content
```

```
-
```

## Errors

* **400 Results not valid** -- The results aren't valid or aren't expressed in the expected way.
* **500 Server error** -- Given when it is impossible to communicate either with the queue or the datastore.

The errors aren't implemented at the time of writing this, so every request will always answer with a 204 status even if there is a problem.

## Example

### Request

```
POST /results
```

```json
{ 
  "a.unique.id.for.the.datum" : { 
    "http://www.modaclouds.eu/rdfs/1.0/monitoringdata#metric" : [ { 
      "type" : "literal" ,
      "value" : "FrontendCPUUtilization"
    }
     ] ,
    "http://www.modaclouds.eu/rdfs/1.0/monitoringdata#timestamp" : [ { 
      "type" : "literal" ,
      "value" : "1409223851698" ,
      "datatype" : "http://www.w3.org/2001/XMLSchema#integer"
    }
     ] ,
    "http://www.modaclouds.eu/rdfs/1.0/monitoringdata#value" : [ { 
      "type" : "literal" ,
      "value" : "0.11416644992590952e0" ,
      "datatype" : "http://www.w3.org/2001/XMLSchema#double"
    }
     ] ,
    "http://www.modaclouds.eu/rdfs/1.0/monitoringdata#resourceId" : [ { 
      "type" : "literal" ,
      "value" : "frontend1" ,
      "datatype" : "http://www.w3.org/2001/XMLSchema#string"
    }
     ]
  }
}
```

### Response

```
Status: 204 No Content
```

```
-
```

