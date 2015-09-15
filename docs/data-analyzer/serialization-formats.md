---
currentMenu: serialization-formats
parentMenu: data-analyzer
---

# Serialization Formats

Available formats:
* `GRAPHITE`
	* format: `resourceId.metric value timestamp`
	* example: `vm1.AverageCPU 0.45 1437468528630`
* `RDF/JSON`
	* format: [RDF 1.1 JSON Alternate Serialization](https://dvcs.w3.org/hg/rdf/raw-file/default/rdf-json/index.html)
	* example:
	```json
	{
	    "_:-5d015db:14eaf9b88de:-75c9": {
	        "http://www.modaclouds.eu/model#timestamp": [
	            {
	                "type": "literal",
	                "value": "1437468528630",
	                "datatype": "http://www.w3.org/2001/XMLSchema#integer"
	            }
	        ],
	        "http://www.modaclouds.eu/model#value": [
	            {
	                "type": "literal",
	                "value": "0.0050000000000000044e0",
	                "datatype": "http://www.w3.org/2001/XMLSchema#double"
	            }
	        ],
	        "http://www.modaclouds.eu/model#resourceId": [
	            {
	                "type": "literal",
	                "value": "vm1",
	                "datatype": "http://www.w3.org/2001/XMLSchema#string"
	            }
	        ],
	        "http://www.modaclouds.eu/model#metric": [
	            {
	                "type": "literal",
	                "value": "AverageCPU",
	                "datatype": "http://www.w3.org/2001/XMLSchema#string"
	            }
	        ],
	        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type": [
	            {
	                "type": "uri",
	                "value": "http://www.modaclouds.eu/model#MonitoringDatum"
	            }
	        ]
	    }
	}
	```
* `TOWER/JSON`
	* format: a simple json object
	* example:
	```json
	{
		"resourceId": "vm1",
		"metric": "AverageCPU",
		"value": 0.45,
		"timestamp": 1437468528630
	}
	```
* `INFLUXDB`
	* format: [InfluxDB json protocol](https://influxdb.com/docs/v0.9/write_protocols/json.html) (**deprecated since 0.9.1**) << update to line protocol required
	* example:
	```json
	[{
		"name":"AverageCpuUtilization",
		"columns":["time","value","resourceId"],
		"points":[
			[1442318984833,16.72875,"10_158_128_15"],
			[1442318984833,2.3862500000000004,"10_158_128_17"],
			[1442318984833,12.826562499999998,"10_158_128_18"],
			[1442318984833,7.961874999999999,"10_158_128_16"],
			[1442318984833,0.9624999999999999,"10_158_128_14"],
			[1442318984833,1.8225000000000002,"10_158_128_11"],
			[1442318984833,5.02875,"10_158_128_12"],
			[1442318984833,8.685,"10_157_128_31"],
			[1442318984833,4.21,"10_157_128_32"],
			[1442318984833,4.1312500000000005,"10_158_128_13"]
		]
	}]
	```