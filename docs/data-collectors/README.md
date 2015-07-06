---
currentMenu: data-collectors
---

# Data Collectors

Data Collectors are *client-side* *self-registering* agents responsible of sending monitoring data to the [Data Analyzer]. They are built with the Cloud in mind, which is dynamic and heterogeneous. A resource being monitored (e.g. a virtual machine) can be shut down as a result of scale down activity, or a new resource can be started as a result of a scale up activity. In both cases, Tower 4Clouds is able to keep its [Model] of the system updated. Data Collectors automatically register and periodically notify their existence and the existence of the resource they monitor. Once a resource is shut down, the [Manager] will take care of unregistering expired resources after the timeout (keep alive period) defined by the data collector itself. Moreover, data collectors are client-side, which means communication is mono-directional, so that really few requirements are requested on the location where they run and collect data. Finally, in order to save traffic (and therefore money) and reduce the overhead, data collectors collect data only when and how requested by the [Manager] according to the [Rules] installed by the user.

Two kind of data collectors have been implemented so far:
- the [Java App Data Collector], which is a Java library that uses aspect oriented programming for collecting application level metrics of your Java application such as Response Time and Throughput,
- a data collector built by another MODAClouds partner (Imperial University), which is an executable Java application able to collect both infrastructure and application level metrics such as CPU Utilization and Application Availability. This data collector can be found [here](https://github.com/imperial-modaclouds/modaclouds-data-collectors).

If you want to build your data collector feel free to contribute. You can find instructions on how to build a data collector [here](byodc.md) and instructions on how to contribute [here](/CONTRIBUTING.md).

What a data collector actually does:
1. Once started, it contacts the [Manager] registering:
	- the resources being monitored and their relationships with the entire [Model],
	- the metrics it is able to collect for each resource
	- the keep alive period, which tells the [Manager] how long to wait an acknowledgement from the data collector before considering it and its resources expired.
2. Periodically, it will contact the [Manager] to acknowledge if any collecting activity is requested for any metric about any resource it is responsible for.
3. Whenever requested, it will start collecting and sending data according to the configuration provided by the [Manager], which contains:
	- the metrics to be collected
	- the resources to be monitored for each metric
	- a list of key value parameters that are data collector specific and specify how the collection should be performed
	- the URL where the data should be sent (\*)


(\*) The URL identifies a [Stream] on the [Data Analyzer]

[Data Analyzer]: ../data-analyzer/
[Java App Data Collector]: java-app-dc.html
[Manager]: ../manager/
[Model]: ../model/
[Rules]: ../rules/
[Stream]: ../data-analyzer/streams.html