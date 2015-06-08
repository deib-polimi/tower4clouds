---
currentMenu: mosaic-installation
---

# Installation on mOSAIC

Dependencies:
- mosaic-rt-jre-7

Download and install using zypper the rpms from the [latest release](https://github.com/deib-polimi/tower4clouds/releases):
```bash
TOWER4CLOUDS_RELEASE=0.2
rm -f /tmp/modaclouds-services-tower4clouds-*-${TOWER4CLOUDS_RELEASE}.noarch.rpm
wget -P /tmp \
	https://github.com/deib-polimi/tower4clouds/releases/download/${TOWER4CLOUDS_RELEASE}/modaclouds-services-tower4clouds-{manager,rdf-history-db}-${TOWER4CLOUDS_RELEASE}.noarch.rpm 
sudo zypper \
		--non-interactive --no-refresh \
		--no-gpg-checks --gpg-auto-import-keys \
   	install \
		/tmp/modaclouds-services-tower4clouds-*-${TOWER4CLOUDS_RELEASE}.noarch.rpm
```
	
Runtime requirements:
- mosaic-components-rabbitmq (required by rdf-history-db)
- apache-fuseki (required by rdf-history-db)

Start it:
```bash
components=(manager rdf-history-db)
TOWER4CLOUDS_RELEASE=0.2-SNAPSHOT
MOS_NODE_PUBLIC_IP="${mos_node_public_ip}"
export \
	MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_PORT=31337 \
	MODACLOUDS_RABBITMQ_ENDPOINT_IP=127.0.0.1 \
	MODACLOUDS_RABBITMQ_ENDPOINT_PORT=21688 \
	MODACLOUDS_FUSEKI_ENDPOINT_IP=127.0.0.1 \
	MODACLOUDS_FUSEKI_ENDPOINT_PORT=3030 \
	MODACLOUDS_FUSEKI_DB_PATH=/ds \
	MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_IP=${MOS_NODE_PUBLIC_IP} \
	MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_IP=${MOS_NODE_PUBLIC_IP} \
	MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_PUBLIC_ENDPOINT_PORT=8175 \
	MODACLOUDS_TOWER4CLOUDS_MANAGER_PUBLIC_ENDPOINT_PORT=8170 \
	MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_IP=127.0.0.1
for component in ${components[@]}
do
	nohup /opt/modaclouds-services-tower4clouds-${component}-${TOWER4CLOUDS_RELEASE}/bin/service-run.bash > /tmp/modaclouds-services-tower4clouds-${component}-${TOWER4CLOUDS_RELEASE}.log 2>&1 &
done
```