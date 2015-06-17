---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `GET` /monitoring-rules

## Description
Returns the list of installed monitoring rules.

***

## URL Parameters

None

***

## Response

**Status:** **200 OK**

**Body:** An XML object with a list of monitoring rules, conforming to the [Monitoring Rules Schema].

***

## Errors

None

***

## Example
**Request**

	GET v1/monitoring-rules

**Response**

	Status: 200 OK

``` xml
<monitoringRules xmlns="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema">
	<monitoringRule id="CPURule"
        startEnabled="true" timeStep="30" timeWindow="30">
        <monitoredTargets>
            <monitoredTarget class="VM" type="Frontend"/>
        </monitoredTargets>
        <collectedMetric metricName="CPUUtilization">
            <parameter name="samplingTime">10</parameter>
            <parameter name="samplingProbability">1</parameter>
        </collectedMetric>
        <metricAggregation aggregateFunction="Average" groupingClass="VM"/>
        <actions>
            <action name="OutputMetric">
                <parameter name="resourceId">ID</parameter>
                <parameter name="metric">AverageCPU</parameter>
                <parameter name="value">METRIC</parameter>
            </action>
        </actions>
    </monitoringRule>
    <monitoringRule id="RAMRule"
        startEnabled="true" timeStep="30" timeWindow="30">
        <monitoredTargets>
            <monitoredTarget class="VM" type="Frontend"/>
        </monitoredTargets>
        <collectedMetric metricName="MemUsed">
            <parameter name="samplingTime">10</parameter>
            <parameter name="samplingProbability">1</parameter>
        </collectedMetric>
        <metricAggregation aggregateFunction="Average" groupingClass="VM"/>
        <actions>
            <action name="OutputMetric">
                <parameter name="resourceId">ID</parameter>
                <parameter name="metric">AverageRAM</parameter>
                <parameter name="value">METRIC</parameter>
            </action>
        </actions>
    </monitoringRule>
</monitoringRules>
```

[Monitoring Rules Schema]: ../../monitoring-rules/
