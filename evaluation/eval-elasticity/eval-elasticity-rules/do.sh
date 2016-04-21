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

if [[ $# -lt 5 ]] ; then
    echo 'Missing arguments: #processes #threads server-address server-user server-ssh-key'
    exit 1
fi

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCRIPTS_DIR=$WD/../../scripts
N_PROCESSES=$1
N_THREADS=$2
SERVER_ADDRESS=$3
SERVER_USER=$4
SERVER_SSH_KEY=$5
MANAGER_PORT=8170

gnudate() {
    if hash gdate 2>/dev/null; then
        gdate "$@"
    else
        date "$@"
    fi
}

timestamp() {
	gnudate +"%T.%3N"
}

log() {
	echo "$(timestamp) $@"
}

niceSleep() {
	for i in $(seq 0 $1); do
	    echo $i
	    sleep 1
	done | $SCRIPTS_DIR/progress-bar -m $1 -w 40 -b "=" -i -t "$2"
	log "$2 - Done"
}


log "Checking if server address $SERVER_ADDRESS is reachable"
ping -c 1 $SERVER_ADDRESS > /dev/null
if [ ! $? = 0 ]; then
	log "Server address $SERVER_ADDRESS is not reachable"
	exit 1
fi
ssh -q -i "$SERVER_SSH_KEY" ${SERVER_USER}@${SERVER_ADDRESS} exit
if [ ! $? = 0 ]; then
	log "Cannot ssh to ${SERVER_ADDRESS}"
	exit 1
fi

N_CLIENTS=$(($N_PROCESSES*$N_THREADS))
log "Number of clients = ${N_CLIENTS}"
RES_FOLDER=${WD}/results/$N_CLIENTS
rm -rf $RES_FOLDER
mkdir -p $RES_FOLDER

log "Restarting remote MS and DA"
ssh -T -q -o LogLevel=Error -i "$SERVER_SSH_KEY" \
	${SERVER_USER}@${SERVER_ADDRESS} "pkill java
		export DATA_ANALYZER_HOME=/usr/local/data-analyzer-0.3.1
		nohup data-analyzer > \
			/var/log/data-analyzer.log 2>&1 &
		export MANAGER_HOME=/usr/local/manager-server-0.3.1
		nohup manager-server -daip ${SERVER_ADDRESS} > \
			/var/log/manager-server.log 2>&1 &"

niceSleep 20 "Manager and DA are booting up"



i="0"
while [ $i -lt $N_PROCESSES ]
do
	java -jar $WD/target/eval-elasticity-rules-*-jar-with-dependencies.jar \
		-process $i \
		-threads $N_THREADS \
		-manager-addr $SERVER_ADDRESS 2> $RES_FOLDER/dc_${i}.log &
	DCS_PIDS="${!} ${DCS_PIDS}"
	i=$[$i+1]
done

niceSleep $(echo $N_CLIENTS | awk '{printf "%.0f", log($1)/log(10)*60+10}') "Data collectors are booting up"

log "Installing rule"
curl -X POST -d @- $SERVER_ADDRESS:$MANAGER_PORT/v1/monitoring-rules << EOF
<monitoringRules xmlns="http://www.modaclouds.eu/xsd/1.0/monitoring_rules_schema">
    <monitoringRule id="CPURule"
        startEnabled="true" timeStep="10" timeWindow="60">
        <monitoredTargets>
            <monitoredTarget class="VM"/>
        </monitoredTargets>
        <collectedMetric metricName="CPUUtilization">
        </collectedMetric>
        <metricAggregation aggregateFunction="Average" groupingClass="VM"/>
        <actions>
            <action name="OutputMetric">
                <parameter name="resourceId">ID</parameter>
                <parameter name="metric">AverageCPU</parameter>
                <parameter name="value">METRIC</parameter>
            </action>
        </actions>
    </monitoringRule>
</monitoringRules>
EOF
log "Rule installed"
RULE_INSTALLED=$(timestamp)

niceSleep 300 "Waiting for data collectors to download their configuration"

log "Uninstalling rule"
curl -X DELETE $SERVER_ADDRESS:$MANAGER_PORT/v1/monitoring-rules/CPURule
log "Rule uninstalled"
RULE_UNINSTALLED=$(timestamp)

niceSleep 300 "Waiting for data collectors to download their configuration"

log "Merging DC log files"
cat $RES_FOLDER/dc_*.log > $RES_FOLDER/dc.log
rm $RES_FOLDER/dc_*.log

log "Retrieving server logs"
scp -i "$SERVER_SSH_KEY" -q ${SERVER_USER}@${SERVER_ADDRESS}:/var/log/manager-server.log $RES_FOLDER/man.log
scp -i "$SERVER_SSH_KEY" -q ${SERVER_USER}@${SERVER_ADDRESS}:/var/log/data-analyzer.log $RES_FOLDER/da.log


for i in `seq 0 $(($N_CLIENTS-1))`
do
	echo "$i,$RULE_INSTALLED"
done > $RES_FOLDER/rule-installed.csv

for i in `seq 0 $(($N_CLIENTS-1))`
do
	echo "$i,$RULE_UNINSTALLED"
done > $RES_FOLDER/rule-uninstalled.csv

grep -i "should start sending metrics" $RES_FOLDER/dc.log | awk -F' ' '{print $7","$1}' | tail -r | awk -F, '!seen[$1]++' > $RES_FOLDER/dc-start-sending.csv
grep -i "should stop sending metrics" $RES_FOLDER/dc.log | awk -F' ' '{print $7","$1}' | tail -r | awk -F, '!seen[$1]++' > $RES_FOLDER/dc-stop-sending.csv

echo "id,rule-installed,dc-start-sending,rule-uninstalled,dc-stop-sending" > $RES_FOLDER/join.csv
join -t, \
	<(sort -t, -k 1 -n $RES_FOLDER/rule-installed.csv) \
	<(sort -t, -k 1 -n $RES_FOLDER/dc-start-sending.csv) \
	| join -t, - \
	<(sort -t, -k 1 -n $RES_FOLDER/rule-uninstalled.csv) \
	| join -t, - \
	<(sort -t, -k 1 -n $RES_FOLDER/dc-stop-sending.csv) \
	>> $RES_FOLDER/join.csv


log "Killing DCS"
kill $DCS_PIDS

log "Done"