---
currentMenu: historydb-datastore
parentMenu: historydb
parent2Menu: observers
---

[&#9664; Back to list](.)

# Add Monitoring Data

When adding a new monitoring datum to the system (using the REST API [POST-monitoring-data](../api/POST-monitoring-data.md)), a number of triples are inserted in the datastore.

Let's assume that the element exposes:

* a timestamp, e.g. `1425399022110`, corresponding to the date *Tue, 03 Mar 2015 17:10:22 GMT+1:00*,
* an unique id, e.g. `b499a3d3-248a-4bbe-859c-e345d47ed479`.

A graph with the name associated to the hour in which the timestamp is contained (e.g. `http://www.modaclouds.eu/historydb/monitoring-data/1425398400000` for the example above, where `1425398400000` corresponds to the date *Tue, 03 Mar 2015 17:00:00 GMT+1:00*) is created or used if it already exists.
The triples added have all the same subject, the unique id (that here is written as `<mo:id>`, so e.g. `<mo:b499a3d3-248a-4bbe-859c-e345d47ed479>`), and the predicates and objects couples added are:

* `metric` and the actual metric name,
* `timestamp`, and the timestamp,
* `resourceId`, and the resource ID,
* `value`, and the actual value of the metric for the resource with the given ID.

All the predicates have the `http://www.modaclouds.eu/rdfs/1.0/monitoringdata#` prefix (so e.g. for value we'll have `http://www.modaclouds.eu/rdfs/1.0/monitoringdata#value`).

Lastly, in the `default` graph, one tuple is added, with:

* the subject that is the URI of the graph (e.g. `http://www.modaclouds.eu/historydb/monitoring-data/1425398400000`),
* the predicate is the constant `mo:timestamp`, and
* the object is the hour in which the timestamp is contained (e.g. `1425398400000` for the example above, where `1425398400000` corresponds to the date *Tue, 03 Mar 2015 17:00:00 GMT+1:00*).


## Examples of Query

Here are some examples of SPARQL query:

* find all the monitoring data inserted:

```sparql
SELECT * {
    ?s <mo:timestamp> ?o
    FILTER (regex(str(?s), "monitoring"))
}
```

* find all the monitoring data inserted in a specific hourly timestamp:

```sparql
SELECT * {
    ?s <mo:timestamp> ?o 
    FILTER (?o = 1425574800000 && regex(str(?s), "monitoring"))
}
```

* find all the monitoring data inserted in a range of hourly timestamps:

```sparql
SELECT * {
    ?s <mo:timestamp> ?o
    FILTER (?o >= 1425574600000 && ?o <= 1425574900000 && regex(str(?s), "monitoring"))
}
```

* find all the metrics registered:

```sparql
SELECT DISTINCT ?o
WHERE {
    GRAPH ?g { ?s <http://www.modaclouds.eu/rdfs/1.0/monitoringdata#metric> ?o }
}
```

* find all the graphs and subjects for a specific metric (e.g. `FrontendCPUUtilization`):

```sparql
SELECT ?g ?s
WHERE {
    GRAPH ?g { ?s <http://www.modaclouds.eu/rdfs/1.0/monitoringdata#metric> "FrontendCPUUtilization" }
}
```

* find all data for a specific metric (e.g. `FrontendCPUUtilization`):

```sparql
SELECT ?g ?s ?p ?o
WHERE {
    GRAPH ?g { ?s ?p ?o }
    GRAPH ?g { ?s <http://www.modaclouds.eu/rdfs/1.0/monitoringdata#metric> "FrontendCPUUtilization" }
}
```
