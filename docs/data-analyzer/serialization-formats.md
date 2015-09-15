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