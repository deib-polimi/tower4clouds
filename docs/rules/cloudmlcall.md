---
currentMenu: cloudmlcall
parentMenu: actions
parent2Menu: rules
---

# CloudML Call

The CloudML Call action issues a command on a running CloudML daemon. For it to be working, then, there is to be a running daemon *before* registering the rule (as it checks when registering it if it's available or not).

## Parameters

Name | Default value | Explanation
--- | --- | ---
`ip` | 127.0.0.1 | IP of the CloudML daemon
`port` | 9030 | Port of the CloudML daemon
`command` | | Command that will be issued to CloudML (possible values: `SCALE`, `BURST`)
`tier` | | Name of the tier that will be considered, as defined in the CloudML deploy model
`n` | 1 | Number of instances that will be considered (also negative numbers are allowed)
`cooldown` | 600 | The period (in seconds) on which the action will have to stay disabled after being issued

Please notice that all the parameters with a default value could be omitted when registering the rule.

## Example

This rule will scale up of 1 instance of the `HTTPAgent` when the `CPUUtilization` metric reaches the 0.8 threshold. After requesting the scale up, it will disable the rule for the cooldown period of 600 seconds (because that's what it takes to scale up on the provider it is being used), and enable it again right after that. The CloudML daemon is running on the same machine where the monitoring server is, and the port it is using is the default one, 9030.

```xml
<monitoringRules xmlns="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema">
  <monitoringRule id="cpuUtilizationAboveRule" timeStep="180" timeWindow="180">
    <monitoredTargets>
      <monitoredTarget class="VM" type="Frontend"/>
    </monitoredTargets>
    <collectedMetric metricName="CPUUtilization">
      <parameter name="samplingProbability">1</parameter>
      <parameter name="samplingTime">180</parameter>
    </collectedMetric>
    <metricAggregation aggregateFunction="Average" groupingClass="CloudProvider"/>
    <condition>METRIC &gt;= 0.8</condition>
    <actions>
      <action name="CloudMLCall">
        <parameter name="ip">127.0.0.1</parameter>
        <parameter name="port">9030</parameter>
        <parameter name="command">SCALE</parameter>
        <parameter name="tier">HTTPAgent</parameter>
        <parameter name="n">1</parameter>
        <parameter name="cooldown">600</parameter>
      </action>
    </actions>
  </monitoringRule>
</monitoringRules>
```

Another example, calling directly the `BURST` command and skipping all the parameters for which we want to use the defaults is this one:

```xml
<monitoringRules xmlns="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema">
  <monitoringRule id="cpuUtilizationAboveRule2" timeStep="180" timeWindow="180">
    <monitoredTargets>
      <monitoredTarget class="VM" type="Frontend"/>
    </monitoredTargets>
    <collectedMetric metricName="CPUUtilization">
      <parameter name="samplingProbability">1</parameter>
      <parameter name="samplingTime">180</parameter>
    </collectedMetric>
    <metricAggregation aggregateFunction="Average" groupingClass="CloudProvider"/>
    <condition>METRIC &gt;= 0.95</condition>
    <actions>
      <action name="CloudMLCall">
        <parameter name="command">BURST</parameter>
        <parameter name="tier">HTTPAgent</parameter>
      </action>
    </actions>
  </monitoringRule>
</monitoringRules>
```

## Example of CloudML Deploy Model

A deploy model that would work with the rules above is the one provided here, where two providers are being used (Flexiant and Amazon). To specify which one is the matching machine type on both the providers, this name pattern is used: both vms have the same prefix, `HTTPAgent`, but each of them has a suffix that is `@Provider`, either `@Amazon` or `@Flexiant` then. In the rule above, we used only the common prefix, `HTTPAgent`, as the name of the tier.

That is the only thing needed for the action to work, you can keep using the CloudML model you were already using. Of course, if you're going to use just one provider, you don't need to abide to this name convention.

```json
{
  "eClass": "net.cloudml.core:CloudMLModel",
  "name": "CloudMLCallTest",
  "providers": [
    {
      "eClass": "net.cloudml.core:Provider",
      "credentials": "credentialsAmazon.properties",
      "name": "aws-ec2",
      "properties": [
        {
          "eClass": "net.cloudml.core:Property",
          "name": "MaxVMs",
          "value": "5"
        }
      ]
    },
    {
      "eClass": "net.cloudml.core:Provider",
      "credentials": "credentialsFlexiant.properties",
      "name": "flexiant",
      "properties": [
        {
          "eClass": "net.cloudml.core:Property",
          "name": "MaxVMs",
          "value": "5"
        },
        {
          "eClass": "net.cloudml.core:Property",
          "name": "endPoint",
          "value": "https://api.sd1.flexiant.net:4442/userapi"
        }
      ]
    }
  ],
  "internalComponents": [
    {
      "eClass": "net.cloudml.core:InternalComponent",
      "name": "app",
      "resources": [
        {
          "eClass": "net.cloudml.core:Resource",
          "name": "startApp",
          "downloadCommand": "cd ~ && curl -O https://raw.githubusercontent.com/deib-polimi/modaclouds-tests/master/machines/downloadEverything.sh && bash /home/ubuntu/downloadEverything.sh HTTPAgent",
          "installCommand": "bash /home/ubuntu/installEverything",
          "startCommand": "sudo bash /home/ubuntu/startHTTPAgent [mmIp] [mmPort]",
        }
      ],
      "requiredExecutionPlatform": {
        "eClass": "net.cloudml.core:RequiredExecutionPlatform",
        "name": "appRequired",
        "owner": "internalComponents[app]"
      },
      "properties" : [
        {
          "eClass": "net.cloudml.core:Property",
          "name": "env:MODACLOUDS_TOWER4CLOUDS_CLOUD_PROVIDER_ID",
          "value": "${this.provider.id}"
        }
      ]
    }
  ],
  "internalComponentInstances": [
    {
      "eClass": "net.cloudml.core:InternalComponentInstance",
      "name": "appInstance",
      "type": "internalComponents[app]",
      "requiredExecutionPlatformInstance": {
        "eClass": "net.cloudml.core:RequiredExecutionPlatformInstance",
        "name": "appRequiredInstance",
        "owner": "internalComponentInstances[appInstance]",
        "type": "internalComponents[app]/requiredExecutionPlatform[appRequired]"
      }
    }
  ],
  "vms": [
    {
      "eClass": "net.cloudml.core:VM",
      "is64os": true,
      "location": "us-west-1",
      "providerSpecificTypeName": "m3.large",
      "minRam": "7680",
      "maxRam": "0",
      "minCores": "2",
      "maxCores": "0",
      "minStorage": "8",
      "maxStorage": "0",
      "name": "HTTPAgent@Amazon",
      "os": "ubuntu",
      "privateKey": "desantis-ireland.pem",
      "provider": "providers[aws-ec2]",
      "securityGroup": "default",
      "sshKey": "desantis-ireland",
      "providedExecutionPlatforms": [
        {
          "eClass": "net.cloudml.core:ProvidedExecutionPlatform",
          "name": "HTTPAgentTIERAmazon",
          "owner": "vms[HTTPAgent@Amazon]",
          "offers": [
            {
              "eClass": "net.cloudml.core:Property",
              "name": "OS",
              "value": "Ubuntu"
            }
          ]
        }
      ]
    },
    {
      "eClass": "net.cloudml.core:VM",
      "imageId": "Ubuntu 14.04 (Cluster Two)",
      "is64os": true,
      "minRam": "4000",
      "maxRam": "0",
      "minCores": "2",
      "maxCores": "0",
      "minStorage": "50",
      "maxStorage": "0",
      "name": "HTTPAgent@Flexiant",
      "os": "ubuntu",
      "privateKey": "polimi-review-2014.pem",
      "groupName": "Polimi CEPH Cluster",
      "provider": "providers[flexiant]",
      "securityGroup": "all",
      "sshKey": "polimi-review-2014",
      "providedExecutionPlatforms": [
        {
          "eClass": "net.cloudml.core:ProvidedExecutionPlatform",
          "name": "HTTPAgentTIERFlexiant",
          "owner": "vms[HTTPAgent@Flexiant]",
          "offers": [
            {
              "eClass": "net.cloudml.core:Property",
              "name": "OS",
              "value": "Ubuntu"
            }
          ]
        }
      ]
    }
  ],
  "vmInstances": [
    {
      "eClass": "net.cloudml.core:NodeInstance",
      "name": "HTTPAgentInstance",
      "type": "vms[HTTPAgent@Flexiant]",
      "providedExecutionPlatformInstances": [
        {
          "eClass": "net.cloudml.core:ProvidedExecutionPlatformInstance",
          "name": "HTTPAgentTier",
          "owner": "vmInstances[HTTPAgentInstance]",
          "type": "vms[HTTPAgent@Flexiant]/providedExecutionPlatforms[HTTPAgentTIERFlexiant]"
        }
      ]
    }
  ],
  "executesInstances": [
    {
      "eClass": "net.cloudml.core:ExecuteInstance",
      "name": "runApp",
      "providedExecutionPlatformInstance": "vmInstances[HTTPAgentInstance]/providedExecutionPlatformInstances[HTTPAgentTier]",
      "requiredExecutionPlatformInstance": "internalComponentInstances[appInstance]/requiredExecutionPlatformInstance[appRequiredInstance]"
    }
  ]
}
```
