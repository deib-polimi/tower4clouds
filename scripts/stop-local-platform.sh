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

log 'Stopping Manager, Data Analyzer and RDF History DB'
pkill -f tower4clouds

log 'Stopping VM with rabbitMQ and Fuseki'
cd ${TOWER_ROOT}/observers/rdf-history-db
vagrant halt

log 'Stopping VM with Influxdb Graphite and Grafana'
cd ${TOWER_ROOT}/observers/influxdb-graphite-grafana
vagrant halt
