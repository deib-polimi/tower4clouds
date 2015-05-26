#!/bin/bash 
function log {
	printf "\033[1;36m$1\033[0m\n"
}

cd ..
TOWER_ROOT=`pwd`

log 'Starting rabbitMQ and Fuseki inside a VM with Vagrant'
cd ${TOWER_ROOT}/observers/rdf-history-db
vagrant up

log 'Starting Influxdb Graphite and Grafana inside a VM with Vagrant'
cd ${TOWER_ROOT}/observers/influxdb-graphite-grafana
vagrant up

log 'Starting the Data Analyzer'
cd ${TOWER_ROOT}/data-analyzer/target/
tar xf data-analyzer-*.tar.gz
cd data-analyzer-*
./tower4clouds-data-analyzer > da.log 2>&1 &

log 'Starting the RDF History DB'
cd ${TOWER_ROOT}/observers/rdf-history-db/rdf-history-db-main/target/
tar xf rdf-history-db-*.tar.gz
cd rdf-history-db-*
./tower4clouds-rdf-history-db > hdb.log 2>&1 &

log 'Starting the Manager'
cd ${TOWER_ROOT}/manager/manager-server/target/
tar xf manager-server-*.tar.gz
cd manager-server-*
./tower4clouds-manager -rdf-history-db-ip 127.0.0.1 > mgr.log 2>&1 &