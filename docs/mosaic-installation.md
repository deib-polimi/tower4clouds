---
currentMenu: mosaic-installation
---

# Installation on mOSAIC

Set current MODAClouds version number:
```bash
MODACLOUDS_RELEASE=0.7.0.13
```

## Requirements
Dependencies:
- mosaic-rt-jre-7

Required components:
- mosaic-components-rabbitmq (required by rdf-history-db)
	* install via
		```bash
		sudo zypper \
				--non-interactive --no-refresh \
				--no-gpg-checks --gpg-auto-import-keys \
			install mosaic-components-rabbitmq
		```
	* run via
		```bash
		nohup /opt/mosaic-components-rabbitmq-0.7.0.11/bin/mosaic-components-rabbitmq--run-component > \
			/tmp/mosaic-components-rabbitmq.log 2>&1 &
		```
	* stop via
		```bash
		???
		```
- modaclouds-services-fuseki (required by rdf-history-db)
	* install via
		```bash
		sudo zypper \
				--non-interactive --no-refresh \
				--no-gpg-checks --gpg-auto-import-keys \
			install modaclouds-services-fuseki
		```
	* run via
		```bash
		env \
			MODACLOUDS_FUSEKI_ENDPOINT_PORT=3030 \
			MODACLOUDS_FUSEKI_DATASET_PATH=/ds \
		nohup /opt/modaclouds-services-fuseki-${MODACLOUDS_RELEASE}/bin/modaclouds-services-fuseki--run-component > \
			/tmp/modaclouds-services-fuseki.log 2>&1 &
		```
	* stop via
		```bash
		???
		```
- modaclouds-services-metric-explorer (Graphite) (required to explore data at runtime)
	* install via
		```bash
		sudo zypper \
				--non-interactive --no-refresh \
				--no-gpg-checks --gpg-auto-import-keys \
			install modaclouds-services-metric-explorer
		```
	* run via
		```bash
		MOS_NODE_PUBLIC_IP="${mos_node_public_ip}" # CHECK THIS VAR IS ASSIGNED
		env \
	        MODACLOUDS_METRIC_EXPLORER_DASHBOARD_ENDPOINT_IP="${MOS_NODE_PUBLIC_IP}" \
	        MODACLOUDS_METRIC_EXPLORER_DASHBOARD_ENDPOINT_PORT=9010 \
	        MODACLOUDS_METRIC_EXPLORER_QUERY_ENDPOINT_IP="${MOS_NODE_PUBLIC_IP}" \
	        MODACLOUDS_METRIC_EXPLORER_QUERY_ENDPOINT_PORT=9011 \
	        MODACLOUDS_METRIC_EXPLORER_PICKLE_RECEIVER_ENDPOINT_IP="${MOS_NODE_PUBLIC_IP}" \
	        MODACLOUDS_METRIC_EXPLORER_PICKLE_RECEIVER_ENDPOINT_PORT=9012 \
	        MODACLOUDS_METRIC_EXPLORER_LINE_RECEIVER_ENDPOINT_IP="${MOS_NODE_PUBLIC_IP}" \
	        MODACLOUDS_METRIC_EXPLORER_LINE_RECEIVER_ENDPOINT_PORT=9013 \
	    nohup /opt/modaclouds-services-metric-explorer-${MODACLOUDS_RELEASE}/bin/modaclouds-services-metric-explorer--run-service > \
	    	/tmp/modaclouds-services-metric-explorer-${MODACLOUDS_RELEASE}.log 2>&1 &
		```
	* stop via
		```bash
		???
		```

## Installation of Tower 4Clouds

```bash
sudo zypper \
		--non-interactive --no-refresh \
		--no-gpg-checks --gpg-auto-import-keys \
   	install \
		modaclouds-services-tower4clouds-manager \
		modaclouds-services-tower4clouds-data-analyzer \
		modaclouds-services-tower4clouds-rdf-history-db
```


## Start Tower 4Clouds
On a single machine in one go:
```bash
components=(data-analyzer manager rdf-history-db)
MOS_NODE_PUBLIC_IP="${mos_node_public_ip}" # CHECK THIS VAR IS ASSIGNED
export \
	MODACLOUDS_RABBITMQ_ENDPOINT_IP=127.0.0.1 \
	MODACLOUDS_RABBITMQ_ENDPOINT_PORT=21688 \
	MODACLOUDS_FUSEKI_ENDPOINT_IP=127.0.0.1 \
	MODACLOUDS_FUSEKI_ENDPOINT_PORT=3030 \
	MODACLOUDS_FUSEKI_DB_PATH=/ds \
	MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_IP=${MOS_NODE_PUBLIC_IP} \
	MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_IP=${MOS_NODE_PUBLIC_IP} \
	MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT=8175 \
	MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_PORT=8170 \
	MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_IP=127.0.0.1 \
	MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_PORT=31337
for component in ${components[@]}
do
	nohup /opt/modaclouds-services-tower4clouds-${component}-${MODACLOUDS_RELEASE}/bin/modaclouds-services-tower4clouds-${component}--run-service > \
		/tmp/modaclouds-services-tower4clouds-${component}-${MODACLOUDS_RELEASE}.log 2>&1 &
done
```

One component at a time:
```bash
MOS_NODE_PUBLIC_IP="${mos_node_public_ip}" # CHECK THIS VAR IS ASSIGNED
env \
	MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT=8175 \
	nohup /opt/modaclouds-services-tower4clouds-data-analyzer-${MODACLOUDS_RELEASE}/bin/modaclouds-services-tower4clouds-data-analyzer--run-service > \
		/tmp/modaclouds-services-tower4clouds-data-analyzer-${MODACLOUDS_RELEASE}.log 2>&1 &
#
env \
	MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_IP=${MOS_NODE_PUBLIC_IP} \
	MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_IP=${MOS_NODE_PUBLIC_IP} \
	MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT=8175 \
	MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_PORT=8170 \
	MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_IP=127.0.0.1
	MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_PORT=31337 \
	nohup /opt/modaclouds-services-tower4clouds-manager-${MODACLOUDS_RELEASE}/bin/modaclouds-services-tower4clouds-manager--run-service > \
		/tmp/modaclouds-services-tower4clouds-manager-${MODACLOUDS_RELEASE}.log 2>&1 &
#
env \
	MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_PORT=31337 \
	MODACLOUDS_RABBITMQ_ENDPOINT_IP=127.0.0.1 \
	MODACLOUDS_RABBITMQ_ENDPOINT_PORT=21688 \
	MODACLOUDS_FUSEKI_ENDPOINT_IP=127.0.0.1 \
	MODACLOUDS_FUSEKI_ENDPOINT_PORT=3030 \
	MODACLOUDS_FUSEKI_DB_PATH=/ds \
	nohup /opt/modaclouds-services-tower4clouds-rdf-history-db-${MODACLOUDS_RELEASE}/bin/modaclouds-services-tower4clouds-rdf-history-db--run-service > \
		/tmp/modaclouds-services-tower4clouds-rdf-history-db-${MODACLOUDS_RELEASE}.log 2>&1 &
```

Shut down Tower 4Clouds:
```bash
pkill -f tower4clouds
```