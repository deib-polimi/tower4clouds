---
currentMenu: flexiant-dc
parentMenu: data-collectors
---

#Flexiant Data Collector

## Provided Metrics

|Metric Name|Target Class|Required Parameters|Description|
|-----------|------------|-------------------|-----------|
|CPUUtilization|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the CPU Utilization of every node by parsing a remote file which url is specified in the configuration properties file.<br/>The time interval between two samples is specified by the parameter sampling time. |
|RamUsage|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the Ram Utilization of every node.<br/>Like the CPUUtilization metric, this metrics parse a remote file to retrive the data sample.|
|NodeLoadMetric|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the load of every node.<br/>Like the CPUUtilization metric, this metrics parse a remote file to retrive the data sample.|
|TXNetworkMetric|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the band usage of TX network of every node.<br/>Like the CPUUtilization metric, this metrics parse a remote file to retrive the data sample.|
|RXNetworkMetric|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the band usage of RX network of every node.<br/>Like the CPUUtilization metric, this metrics parse a remote file to retrive the data sample.|
|StorageCluster|Cluster|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the used storage of every cluster. Unlike nodes, clusters are not retrived from a remote file, they are fixed in the source code of DC (there are Cluster1 and Cluster2). <br/> The samples are collected in the same way of CPUUtilization metric.|
|RackLoad|Rack|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the measured energy load of every rack. To retrieve racks ids the DC parse the same remote file that contains values of the metric. <br/> The samples are collected in the same way of CPUUtilization metric.|

##Resources Identification
Every resource in the DC has an id in order to identificate it, the following table shows how the id is determined and the related resources of any kind of resrouce.

|Resource type|ID|Related resources|
|-----------|---------------|--------------------------|
|Node|IP of the node where dots are replaced with the underscores (For example 127.0.0.1 becomes 127_0_0_1)|A node has a set of its related VMs. The id of a VM is its IP (like the node dots are replaced with underscores) |
|Cluster|The id is the word "Cluster" followed by the id number (For example: Cluster1, Custer2...)|A cluster has a set of its related Nodes which ids structure is specficied above.|
|Rack|The id is name of the rack found in the rack load metric's file.|A rack has a set of its related Nodes which ids structure is specficied above.|

## Usage
Flexiant DC is executable but you first have set a few environment variables and create a properties file (specified below) in order to configure the DC.

###Environment Variables
If these variables aren't setted the DC will use tge default value specified in the table.

|Variable Name|Default value (if not setted)|Description|
|-----------|----------------|-------------------|
|MODACLOUDS_TOWER4CLOUDS_MANAGER_IP|localhost|Specify the IP of the Manager which the DC use to estabilish a link to it.|
|MODACLOUDS_TOWER4CLOUDS_MANAGER_PORT|8170|Specify the PORT of the Manager.|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_CONFIG_FILE|/etc/opt/flexiant-nodes-dc/config|Specify the URL of the config file that you must create in order to configure the DC properties.|

###Configuration File
This file must be created before execute DC.
The config file contains a lot of properties that specify URLs of some remote files.<br/>
Below you find the structure of the file with actual working url:

```
#Flexiant DC Properties
URL_NODES=https://cp.sd1.flexiant.net/nodeid/
URL_CPU_METRIC=https://cp.sd1.flexiant.net/nodecpu10/
URL_RAM_METRIC=https://cp.sd1.flexiant.net/noderam10/
URL_NODELOAD_METRIC=https://cp.sd1.flexiant.net/nodeload10/
URL_TXNETWORK_METRIC=https://cp.sd1.flexiant.net/nodenet10/
URL_RXNETWORK_METRIC=https://cp.sd1.flexiant.net/nodenet10/
URL_STORAGE_METRIC=https://cp.sd1.flexiant.net/storage10/
URL_RACKLOAD_METRIC=https://cp.sd1.flexiant.net/rackload10/upsload.csv
URL_VMS=https://cp.sd1.flexiant.net/VMPlacement/FCOVMPlacement.csv
```

The first property specify URL where the DC can find files which contain nodes of every cluster.<br/>
The other properties specify URL of files which contain metrics' samples.

###Rack/node relations file
If you want you can specify the relationships between racks and nodes you can create a .CSV file which contains that information.
Below there is an example of the structure of the file:

```
Rack,Node,Cluster
A5,10.158.128.15,Cluster2
A5,10.157.128.31,Cluster1
A5,10.158.128.11,Cluster2
A5,10.158.128.16,Cluster2
```
When you run the DC you have to specficy the path of this file as an arguments like the example below:

```
java -jar DC.jar --relations-file pathofthefile
```


###XML rules structure
In order to monitor metrics the remote Manager should has a rule (in its configuration) for every metric you want to collect.<br/>
Below you can see an example of this xml file with one metric configured:

```xml
<monitoringRules xmlns="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema">
    <monitoringRule id="CpuUtilizationRule" startEnabled="true" timeStep="5" timeWindow="5">
        <monitoredTargets>
            <monitoredTarget class="Node" />
        </monitoredTargets>
        <collectedMetric metricName="CPUUtilization">
            <parameter name="samplingTime">10</parameter>
        </collectedMetric>
        <actions>
            <action name="OutputMetric">
                <parameter name="resourceId">ID</parameter>
                <parameter name="metric">CpuUtilization</parameter>
                <parameter name="value">METRIC</parameter>
            </action>
        </actions>
    </monitoringRule>
</monitoringRules>
```

In the example the configured rule is CPUUtilization with a samplingTime of 10 seconds.

## Developers

The source code of the latest release of the Flexiant DC can be found [here](...). 

[type]: ../model/
[manager]: ../manager/