---
currentMenu: rest-api
parentMenu: manager
---

[&#9664; Back to API list](.)


# `POST` /monitoring-rules

## Description
Install monitoring rules.

***

## URL Parameters

None

***

## Data Parameters

An XML object with monitoring rules conforming to the [Monitoring Rules Schema].

***

## Response

**Status:** **204 No Content**

***

## Errors

All known errors cause the resource to return HTTP error code header together with a description of the error.

* **400 Bad Request** - Error while installing monitoring rules: ...

***

## Example
**Request**

	POST v1/monitoring-rules


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
