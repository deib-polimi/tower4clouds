---
currentMenu: byodc
parentMenu: data-collectors
---

#Bring Your Own Data Collector

Please read instructions on how to [contribute][Contributing] first.

## Build a Java Data Collector

If you plan to build a Java Data Collector you can easily create one by using the provided data-collector-library.

Data Collector Library maven dependency:
```xml
<dependency>
    <groupId>it.polimi.tower4clouds</groupId>
    <artifactId>data-collector-library</artifactId>
    <version>VERSION</version>
</dependency>
```
Data Collector Library maven repository:
```xml
<repository>
    <id>deib-polimi-releases</id>
    <url>https://github.com/deib-polimi/deib-polimi-mvn-repo/raw/master/releases</url>
</repository>
```

The communication with Tower 4Clouds platform is throughly managed by the `DCAgent`. Once started, the `DCAgent` will first contact the [Manager], registering itself by sending a `DCDescriptor` object, and then it will keep updating its configuration by periodically contacting the [Manager], with a period specified by the `configSyncPeriod` in the `DCDescriptor` object. The `DCDescriptor` also contains the list of `resources` the data collector is responsible for (i.e., the [data collector sub-model][Model]), which contains both the monitored resources and any other resource related to it, and the `monitoredResourcesByMetric` map, which contains the list of metrics that can be collected by the data collector and the relative resources for which the given metric can be monitored. Finally, the `DCDescriptor` contains a `keepAlive` period which is going to tell the [Manager] the maximum period of time, in seconds, to wait between two subsequent communications with the data collector, after which all `resources` specified by the data collector will expire. If the `configSyncPeriod` is not long enough to prevent `resources` to expire, the `DCAgent` will use the [Keep Alive API](../manager/rest-api/) provided by the [Manager], which allows to consume less data traffic then the [Data Collector Configuration API](../manager/rest-api/) used to retrieve the configuration.

If you want to see examples of data collectors developed using the Data Collector Library, checkout the [Java App DC](https://github.com/deib-polimi/tower4clouds/tree/master/data-collectors/java-app-dc) and the [Imperial Data Collector](https://github.com/imperial-modaclouds/modaclouds-data-collectors) source code.

## Build a non-Java Data Collector

[TODO]

[Contributing]: /Contributing.html
[Manager]: ../manager/
[Model]: ../model/