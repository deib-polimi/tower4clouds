---
currentMenu: flexiant-dc
parentMenu: data-collectors
---

#Flexiant Data Collector

## Provided Metrics

|Metric Name|Target Class|Required Parameters|Description|
|-----------|------------|-------------------|-----------|
|CPUUtilization|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the CPU Utilization (in [0,100]) for the target node specified in the rule with the given sampling time (in seconds). |
|RamUsage|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the Ram Utilization (percentage use [0,100]) for the target node specified in the rule with the given sampling time (in seconds).|
|Load|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the Load (# of running processes) for the target node specified in the rule with the given sampling time (in seconds).|
|TXNetwork|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the band usage (in bytes/s) of TX network for the target node specified in the rule with the given sampling time (in seconds).|
|RXNetwork|Node|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the band usage (in bytes/s) of RX network for the target node specified in the rule with the given sampling time (in seconds).|
|StorageCluster|Cluster|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the used storage (percentage use [0,100]) of every cluster for the target cluster specified in the rule with the given sampling time (in seconds).|
|RackLoad|Rack|<ul><li>samplingTime (default: 60 sec)</li></ul>|Collect the measured energy load (in Ampere) for the target Rack specified in the rule with the given sampling time (in seconds).|

## Resources information and relationships

The information about resources in the Flexiant Testbed (Nodes, Racks and Clusters) are contained in configuration files packaged together the data collector and available [here](https://github.com/deib-polimi/tower4clouds/tree/master/data-collectors/flexiant-nodes-dc/src/main/resources). Such files also contain information about their relationships according to the Tower 4Clouds [meta-model][model]. Custom files can be provided as described in this documentation.

The following conventions were used in the development of this data collectors when the [model] is created:
* Nodes are given their own IP as id, with dots replaced by underscores. Type is provided by the configuration.
* The updated list of VMs contained in a Node is retrieved via the Flexiant provided API. **The important assumption here is that VMs ids in the Tower 4Clouds [model] are equal to their IP address, with dots replaced by underscores**. If such assumption is not valid, higher level metrics such as Response Time, collected by other data collectors, cannot be grouped by resources monitored by this data collector (e.g. by Cluster).
* Clusters and Racks are given the id provided by the Flexiant provided API.

## Usage
Flexiant DC can be configured by means of different options (latters replaces the formers):
<ul>
<li>Default configuration</li>
<li>Environment Variables</li>
<li>CLI arguments</li>
</ul>

###Environment Variables
If these variables aren't setted the DC will use the default values specified in the table.

|Variable Name|Default value|Description|
|-----------|----------------|-------------------|
|MODACLOUDS_TOWER4CLOUDS_MANAGER_IP|localhost|Specify the IP of the Manager which the DC use to estabilish a link to it.|
|MODACLOUDS_TOWER4CLOUDS_MANAGER_PORT|8170|Specify the PORT of the Manager.|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_CONFIG_FILE|Internal file|Specify the path of the config file that you must create in order to configure the DC properties (Structure of the file explained below).|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_NODES_FILE|Internal file|Specify the path of the nodes file which contains target nodes. (Structure of the file explained below).|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_CLUSTERS_FILE|Internal file|Specify the path of the clusters file which contains target clusters. (Structure of the file explained below).|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_RACKS_FILE|Internal file|Specify the path of the racks file which contains target racks. (Structure of the file explained below).|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_PLACEMENT_FILE|Internal file|Specify the path of the placement file which determine relationships between racks and nodes, clusters and nodes (Structure of the file explained below).|

###CLI Arguments
CLI Arguments overwrite default value or environment variable for any parameter.

```
Usage: java -jar DC.jar [options]
  Options:
    -managerip
      Manager IP address
    -managerport
      Manager port
    -config-file
      Config file path
    -nodes-file
      Nodes file path
    -clusters-file
      Clusters file path
    -racks-file
      Racks file path
    -placement-file
      Placement file path
```

###Configuration File (config-file)
The config file contains a lot of properties that specify URLs of some remote files.<br/>
Below you find the structure of the file with currently used url:

```
#Flexiant DC Properties
URL_CPU_METRIC=https://cp.sd1.flexiant.net/nodecpu10/
URL_RAM_METRIC=https://cp.sd1.flexiant.net/noderam10/
URL_NODELOAD_METRIC=https://cp.sd1.flexiant.net/nodeload10/
URL_TXNETWORK_METRIC=https://cp.sd1.flexiant.net/nodenet10/
URL_RXNETWORK_METRIC=https://cp.sd1.flexiant.net/nodenet10/
URL_STORAGE_METRIC=https://cp.sd1.flexiant.net/storage10/
URL_RACKLOAD_METRIC=https://cp.sd1.flexiant.net/rackload10/upsload.csv
URL_VMS=https://cp.sd1.flexiant.net/VMPlacement/FCOVMPlacement.csv
```

The properties specify URL of files which contain metrics' samples.

###Nodes, Clusters and Racks file
You can specify the resources which DC have to monitor by create three csv file (nodes, clusters and racks).
Once files are created you have to pass the path of files using one of the ways exaplined above.
Below there is an example of the structure of the files:

```
Id,Type
nodeId,NodeType
```
```
Id,Type
rackId,RackType
```
```
Id,Type
clusterId,ClusterType
```

###Rack/node, Cluster/node relations file (placement-file)
If you want you can specify the relationships between racks and nodes and between clusters and nodes, you can create a .CSV file which contains that information.
Below there is an example of the structure of the file:

```
Rack,Node,Cluster
A5,10.158.128.15,Cluster2
A5,10.157.128.31,Cluster1
A5,10.158.128.11,Cluster2
A5,10.158.128.16,Cluster2
```


### An example of rule
Here we provide an example of [rule][rules] that can be used to monitor the average CPU utilization of all nodes in the Flexiant clusters grouped by Cluster.

```xml
<monitoringRules xmlns="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema">
    <monitoringRule id="CpuUtilizationRule" startEnabled="true" timeStep="5" timeWindow="5">
        <monitoredTargets>
            <monitoredTarget class="Node" />
        </monitoredTargets>
        <collectedMetric metricName="CPUUtilization">
            <parameter name="samplingTime">10</parameter>
        </collectedMetric>
        <metricAggregation aggregateFunction="Average" groupingClass="Cluster"/>
        <actions>
            <action name="OutputMetric">
                <parameter name="resourceId">ID</parameter>
                <parameter name="metric">AverageCPUUtilization</parameter>
                <parameter name="value">METRIC</parameter>
            </action>
        </actions>
    </monitoringRule>
</monitoringRules>

```

[model]: ../model/
[rules]: ../rules/
