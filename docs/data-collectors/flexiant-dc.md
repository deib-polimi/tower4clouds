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


## Usage
Flexiant DC is executable but you first have set a few environment variables and create two files in order to configure the DC.

###Environment Variables
If these variables aren't setted the DC will use tge default value specified in the table.

|Variable Name|Default value (if not setted)|Description|
|-----------|----------------|-------------------|
|MODACLOUDS_TOWER4CLOUDS_MANAGER_IP|localhost|Specify the IP of the Manager which the DC use to estabilish a link to it.|
|MODACLOUDS_TOWER4CLOUDS_MANAGER_PORT|8170|Specify the PORT of the Manager.|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_CONFIG_FILE|/etc/opt/flexiant-nodes-dc/config|Specify the URL of the config file that you must create in order to configure the DC properties.|
|MODACLOUDS_TOWER4CLOUDS_FLEXDC_RULES_FILE|/etc/opt/flexiant-nodes-dc/rules.xml|Specify the URL of the rules.xml file that you must create in order to configure the rules that the DC will pass to the Manager.|

###Configuration Files
These files must be created before execute DC.

####Config file structure
The config file contains a lot of properties that specify URLs of some remote files.<br/>
Below you find the structure of the file with actual working url:

```
#Flexiant DC Properties
URL_NODES_FILE1=https://cp.sd1.flexiant.net/nodeid/Cluster1.csv
URL_NODES_FILE2=https://cp.sd1.flexiant.net/nodeid/Cluster2.csv
URL_CPU_METRIC=https://cp.sd1.flexiant.net/nodecpu10/
URL_RAM_METRIC=https://cp.sd1.flexiant.net/noderam/
URL_NODELOAD_METRIC=https://cp.sd1.flexiant.net/nodeload/
URL_TXNETWORK_METRIC=https://cp.sd1.flexiant.net/nodenet/
URL_RXNETWORK_METRIC=https://cp.sd1.flexiant.net/nodenet/
URL_STORAGE_METRIC=https://cp.sd1.flexiant.net/storage/
```

The first two properties specify URLs of two files which contain the target nodes.<br/>
The other properties specify URL of files which contain metrics' samples, each property needs only if you will create a rule for that metric.

####Rules file structure
This is an xml file which contains rules for the manager, every rule refers to a metric.<br/>
Below you can see an example of this file with one metric configured:

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