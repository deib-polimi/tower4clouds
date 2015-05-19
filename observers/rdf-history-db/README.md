# MODAClouds HistoryDB
In the context of [MODAClouds European project](http://www.modaclouds.eu), the [Politecnico di Milano](http://www.polimi.it) was one of the partners involved in the development of the Monitoring Platform.

HistoryDB is a component that logs in a **datastore** (usually [Apache Fuseki](http://jena.apache.org/documentation/serving_data/index.html)) a number of different requests, making it possible to check the evolution of the system step by step.

A **queue** (usually [RabbitMQ](http://www.rabbitmq.com)) is used for decoupling the sub-component listening to the requests and the sub-component that manages the datastore. 

## Documentation
Useful links to the documentation:
* [User Manual](https://github.com/deib-polimi/modaclouds-history-db/blob/master/doc/user-manual.md)
* [API reference](https://github.com/deib-polimi/modaclouds-history-db/blob/master/doc/api.md)
* [DataStore reference](https://github.com/deib-polimi/modaclouds-history-db/blob/master/doc/datastore.md)
* [Install Guide](https://github.com/deib-polimi/modaclouds-history-db/blob/master/doc/install.md)

## ChangeLog
[v0.1.4](https://github.com/deib-polimi/modaclouds-history-db/blob/master/bin/historydb-0.1.4.zip):

* A subscription is used instead of polling the queue: better performances should be expected.
* Now a fake message is sent at the beginning for each queue handled, so that the code could work with older versions of RabbitMQ.
* Better handling of failures: the fuseki and rabbitmq hosts are checked at the beginning and signaled if they're wrongly configured.

[v0.1.3](https://github.com/deib-polimi/modaclouds-history-db/blob/master/bin/historydb-0.1.3.zip):

* Now an executor service is used also while adding messages to the datastore, fixing all the problems!

[v0.1.2](https://github.com/deib-polimi/modaclouds-history-db/blob/master/bin/historydb-0.1.2.zip):

* Bug fixes.
* Better handling of the queue (maybe fixing some "too many files opened" bug).
* Two new parameters to the script "historydb" for launching fake messages at the beginning of the tool.

[v0.1.1](https://github.com/deib-polimi/modaclouds-history-db/blob/master/bin/historydb-0.1.1.zip):

* Few bug fixes.

[v0.1.0](https://github.com/deib-polimi/modaclouds-history-db/blob/master/bin/historydb-0.1.0.zip):

* First (actually and hopefully) working version. The previous one wasn't saving all the messages received.
* Added some other information in the "default" graph, as described in the datastore guide.

[v0.0.1-SNAPSHOT](https://github.com/deib-polimi/modaclouds-history-db/blob/master/bin/historydb-0.0.1-SNAPSHOT.zip):

* First beta release!
