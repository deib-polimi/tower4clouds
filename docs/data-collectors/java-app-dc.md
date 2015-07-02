---
currentMenu: java-app-dc
parentMenu: data-collectors
---

#Java App Data Collector

## Provided Metrics

|Metric Name|Target Class|Required Parameters|Description|
|-----------|------------|-------------------|-----------|
|ResponseTime|Method|<ul><li>samplingProbability (default: 1)</li></ul>|Collect the response time (in milliseconds) for the target method specified in the monitoring rule with the given probability (in [0,1])|
|EffectiveResponseTime|Method|<ul><li>samplingProbability (default: 1)</li></ul>|Like ResponseTime, but execution time in external calls is subtracted from the total response time|
|Throughput|Method|<ul><li>samplingTime (default: 60)</li></ul>|Collect the throughput (in requests per second) for the target method specified in the monitoring rule with the given sampling time (in seconds)|
|Throughput|InternalComponent|<ul><li>samplingTime (default: 60)</li></ul>|Collect the cumulative throughput (in requests per second) for all monitored methods in the application with the given sampling time (in seconds)|

## Usage

In order to use the library you should first add the java-app-dc library as a dependency in your maven project:

Releases repository:
```xml
<repository>
    <id>deib-polimi-releases</id>
    <url>https://github.com/deib-polimi/deib-polimi-mvn-repo/raw/master/releases</url>
</repository>
```

Dependency:
```xml
<dependency>
	<groupId>it.polimi.tower4clouds</groupId>
	<artifactId>java-app-dc</artifactId>
	<version>VERSION</version>
</dependency>
```

Include in your build life cycle the aspectj plugin:

```xml
<build>
	<plugins>
		<plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>aspectj-maven-plugin</artifactId>
            <version>1.5</version>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>test-compile</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <complianceLevel>1.7</complianceLevel>
                <source>1.7</source>
                <target>1.7</target>
                <aspectLibraries>
                    <aspectLibrary>
                        <groupId>it.polimi.tower4clouds</groupId>
                        <artifactId>java-app-dc</artifactId>
                    </aspectLibrary>
                </aspectLibraries>
            </configuration>
        </plugin>
	</plugins>
</build>
```

When your application starts, the data collector must be configured with the information about the resource it is monitoring, the [manager] endpoint, the package where your java classes are located, and finally started. Let's see an example:
```java
Map<Property, String> applicationProperties = new HashMap<Property, String>();
applicationProperties.put(Property.ID, "App1");
applicationProperties.put(Property.TYPE, "App");
applicationProperties.put(Property.VM_ID, "Frontend1");
applicationProperties.put(Property.VM_TYPE, "Frontend");
applicationProperties.put(Property.CLOUD_PROVIDER_ID, "AWS");
applicationProperties.put(Property.CLOUD_PROVIDER_TYPE, "IaaS");
Registry.initialize(managerIP, managerPort, applicationProperties, "it.polimi.app");
Registry.startMonitoring();
```

Annotate methods you want to monitor, specifying the method [type] as parameter.

```java
@Monitor(type = "register")
private void register() {
	//...
}
```
Annotate methods that perform external calls. The processing time of such methods will be excluded by the EffectiveResponseTime.

```java
@ExternalCall
private void outgoingCall() {
	// write to DB
}
```

Alternatively, the scope of both monitored methods and outgoing calls can be delimited programmatically:

```java
Registry.started("register");
// registration code
AppDataCollectorFactory.startsExternalCall();
// write to DB
AppDataCollectorFactory.endsExternalCall();
// registration code
Registry.ended("register");
```

## Developers

The source code of the latest release of the Java App DC can be found [here](https://github.com/deib-polimi/tower4clouds/tree/master/data-collectors/java-app-dc). 

[type]: ../model/
[manager]: ../manager/