---
currentMenu: get-started
---

# Get Started

Requirements:
- Java 1.7

Installation:
- Download and untar the following packages from the [latest release](https://github.com/deib-polimi/tower4clouds/releases):
	- manager-server-VERSION.tar.gz
	- data-analyzer-VERSION.tar.gz

Start it:
- Start the [Data Analyzer]:
	```bash
	./tower4clouds-data-analyzer > tower4clouds-data-analyzer.log 2>&1 &
	```
- Start the [Manager]:
	```bash
	./tower4clouds-manager > tower4clouds-manager.log 2>&1 &
	```
- Point your browser to `http://<manager-host>:8170/webapp` and you will see the [Manager Webapp].

Your Tower4Clouds Core instance is up, now you need:
1. [Data Collectors] to gather monitoring data,
2. [Rules] to instruct the platform on what to do,
3. [Observers] to visualize or make use of data or events.

Have a look at our [tutorial][Tutorial] to see a complete usage example using Creator 4Clouds and CloudML 4Clouds.

[Manager]: manager/
[Data Analyzer]: data-analyzer/
[Manager Webapp]: manager/webapp.html
[Data Collectors]: data-collectors/
[Rules]: rules/
[Observers]: observers/
[Tutorial]: tutorial
