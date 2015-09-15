#!/bin/bash
#
# Copyright (C) 2014 Politecnico di Milano (marco.miglierina@polimi.it)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

function log {
	printf "\033[1;36m$1\033[0m\n"
}

cd ..
TOWER_ROOT=`pwd`

log 'Starting rabbitMQ and Fuseki inside a VM with Vagrant'
cd ${TOWER_ROOT}/observers/rdf-history-db/fuseki-rabbitmq
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

IP=127.0.0.1
if [ "$#" -ge 1 ];
then
    IP=$1
fi

log 'Starting the Manager'
cd ${TOWER_ROOT}/manager/manager-server/target/
tar xf manager-server-*.tar.gz
cd manager-server-*
./tower4clouds-manager -manip $IP -daip $IP -rdf-history-db-ip 127.0.0.1 > mgr.log 2>&1 &
