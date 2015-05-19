[Documentation table of contents](../toc.md) / [DataStore Reference](../datastore.md) / update-model

# Update Model

When updating a model in the system (using the REST API [POST-model](../rest/model/POST-model.md)), a number of triples are inserted in the datastore.

Let's assume that the sub-component that has to save the model in the datastore received it in a certain time (e.g. with a timestamp `1425399022110`, corresponding to the date *Tue, 03 Mar 2015 17:10:22 GMT+1:00*).

The assumption that no other model is considered at the exact same time is done. Then, a graph with the name containing the timestamp above is created (e.g. `http://www.modaclouds.eu/historydb/model/updates/1425400773874`).

The graph will contain a number of triples, and these are created using the [modaclouds-knowledge-base-api](https://github.com/deib-polimi/modaclouds-knowledge-base-api). All the subjects will have the base prefix `http://www.modaclouds.eu/rdfs/1.0/entities#`.

Another graph with the name associated to the day in which the timestamp is contained (e.g. `http://www.modaclouds.eu/historydb/meta/model/updates/1425340800000` for the example above, where `1425340800000` corresponds to the date *Tue, 03 Mar 2015 00:00:00 GMT+1:00*) is created or used if it already exists.
Only one triple is added, and this has as the subject the name of the graph considered before (e.g. `http://www.modaclouds.eu/historydb/model/updates/1425400773874`), the predicate `<mo:timestamp>` and the actual timestamp as the object of the triple (e.g. `1425400773874`).

Lastly, in the `default` graph, one tuple is added, with:

* the subject that is the URI of the graph (e.g. `http://www.modaclouds.eu/historydb/model/updates/1425400773874`),
* the predicate is the constant `mo:timestamp`, and
* the object is the hour in which the timestamp is contained (e.g. `1425398400000` for the example above, where `1425398400000` corresponds to the date *Tue, 03 Mar 2015 17:00:00 GMT+1:00*).

## Examples of Query

Here are some examples of SPARQL query:

* find all the updates of model inserted:

```sparql
SELECT * {
    ?s <mo:timestamp> ?o
    FILTER (regex(str(?s), "model/updates"))
}
```

* find all the updates of model inserted in a specific hourly timestamp:

```sparql
SELECT * {
    ?s <mo:timestamp> ?o 
    FILTER (?o = 1425574800000 && regex(str(?s), "model/updates"))
}
```

* find all the updates of model inserted in a range of hourly timestamps:

```sparql
SELECT * {
    ?s <mo:timestamp> ?o
    FILTER (?o >= 1425574600000 && ?o <= 1425574900000 && regex(str(?s), "model/updates"))
}
```