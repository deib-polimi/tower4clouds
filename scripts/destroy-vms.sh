#!/bin/bash 
function log {
	printf "\036[1;31m$1\033[0m\n"
}

cd ..
TOWER_ROOT=`pwd`

log 'Destroying VM with rabbitMQ and Fuseki'
cd ${TOWER_ROOT}/observers/rdf-history-db
vagrant destroy -f

log 'Destroying VM with Influxdb Graphite and Grafana'
cd ${TOWER_ROOT}/observers/influxdb-graphite-grafana
vagrant destroy -f