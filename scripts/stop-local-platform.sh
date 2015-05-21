#!/bin/bash 
function log {
	printf "\033[1;36m$1\033[0m\n"
}

cd ..
TOWER_ROOT=`pwd`

log 'Stopping Manager, Data Analyzer and RDF History DB'
pkill -f tower4clouds

log 'Stopping VM with rabbitMQ and Fuseki'
cd ${TOWER_ROOT}/observers/rdf-history-db
vagrant halt

log 'Stopping VM with Influxdb Graphite and Grafana'
cd ${TOWER_ROOT}/observers/influxdb-graphite-grafana
vagrant halt
