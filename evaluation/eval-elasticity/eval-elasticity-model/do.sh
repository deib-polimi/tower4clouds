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

if [[ $# -lt 4 ]] ; then
    echo 'Missing arguments: #processes #threads up/down local/remote [ms address] [ms user] [ms ssh key]'
    exit 1
fi

WD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCRIPTS_DIR=$WD/../../scripts
nThreads=$2
MS_ADDR=$5
MS_USER=$6
MS_SSH_KEY=$7

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


nClients=$(($1*$nThreads))
MODE=$3
if [ $4 = "local" ]; then
	log "Running in local mode"
	pkill -f data-analyzer manager-server
	LOCAL=true
else 
	log "Running in remote mode"
	LOCAL=false
	if [ -z $MS_ADDR ]; then
		log "No manager address specified"
		exit 1
	else
		ping -c 1 $MS_ADDR > /dev/null
		if [ ! $? = 0 ]; then
			log "Ping failed for host ${MS_ADDR}"
			exit 1
		fi
		if [ -z $MS_USER ]; then
			log "No ms user specified"
			exit 1
		fi
		if [ -z "$MS_SSH_KEY" ]; then
			log "No ms ssh key specified"
			exit 1
		fi
		ssh -q -i "$MS_SSH_KEY" ${MS_USER}@${MS_ADDR} exit
		if [ ! $? = 0 ]; then
			log "Cannot ssh to ${MS_ADDR}"
			exit 1
		fi
	fi
fi

log "Number of clients = ${nClients}"
RES_FOLDER=${WD}/results/$nClients/$MODE/$4
rm -rf $RES_FOLDER
mkdir -p $RES_FOLDER


if [ $LOCAL = "true" ]; then

	MS_ADDR="127.0.0.1"
	java -jar ../../../data-analyzer/target/data-analyzer-*-jar-with-dependencies.jar 2> $RES_FOLDER/da.log &
	DA_PID=$!
	java -jar ../../../manager/manager-server/target/manager-server-*-jar-with-dependencies.jar 2> $RES_FOLDER/man.log &
	MS_PID=$!
else
	log "Restarting remote MS and DA"
	ssh -T -q -o LogLevel=Error -i "$MS_SSH_KEY" \
		${MS_USER}@${MS_ADDR} "pkill java
			export DATA_ANALYZER_HOME=/usr/local/data-analyzer-0.3.1
			nohup data-analyzer > \
				/var/log/data-analyzer.log 2>&1 &
			export MANAGER_HOME=/usr/local/manager-server-0.3.1
			nohup manager-server -daip ${MS_ADDR} > \
				/var/log/manager-server.log 2>&1 &"
fi
niceSleep 20 "Manager and DA are booting up"



i="0"
while [ $i -lt $1 ]
do
	java -jar $WD/target/eval-elasticity-model-*-jar-with-dependencies.jar \
		-process $i \
		-threads $nThreads \
		-manager-addr $MS_ADDR 2> $RES_FOLDER/dc_${i}.log &
	DCS_PIDS="${!} ${DCS_PIDS}"
	i=$[$i+1]
done

niceSleep $(echo $nClients | awk '{printf "%.0f", log($1)/log(10)*60+10}') "Data collectors are booting up"

if [ $LOCAL = "false" ] || [ $MODE = "down" ]; then
	log "Killing DCS"
	kill $DCS_PIDS
	DC_KILLED_TS=$(timestamp)
	echo $DC_KILLED_TS > $RES_FOLDER/dc-killed.csv
else
	log "Killing DA, MS and DCS"
	kill $DA_PID $MS_PID $DCS_PIDS
fi

if [ $MODE = "down" ]; then
	niceSleep $(echo $nClients | awk '{printf "%.0f", log($1)/log(10)*60+100}') "Waiting for model to be updated"
fi

cat $RES_FOLDER/dc_*.log > $RES_FOLDER/dc.log
rm $RES_FOLDER/dc_*.log

if [ $LOCAL = false ]; then
	log "Retrieving logs"
	scp -i "$MS_SSH_KEY" -q ${MS_USER}@${MS_ADDR}:/var/log/manager-server.log $RES_FOLDER/man.log
	scp -i "$MS_SSH_KEY" -q ${MS_USER}@${MS_ADDR}:/var/log/data-analyzer.log $RES_FOLDER/da.log
fi

if [ $MODE = "up" ]; then
	grep -i "started" $RES_FOLDER/dc.log | awk -F' ' '{print $8","$1}' | awk -F, '!seen[$1]++' > $RES_FOLDER/dc-started.csv
	grep -i "adding" $RES_FOLDER/man.log | sed 's/\([^ ]*\).*id=\(.*\)]].*/\2,\1/' | awk -F, '!seen[$1]++' > $RES_FOLDER/man-adding.csv
	grep -i "added" $RES_FOLDER/man.log | sed 's/\([^ ]*\).*id=\(.*\)]].*/\2,\1/' | awk -F, '!seen[$1]++' > $RES_FOLDER/man-added.csv
	grep -i "dc descriptor registered" $RES_FOLDER/dc.log | sed 's/\([^ ]*\).*id=\(.*\)]].*/\2,\1/' | awk -F, '!seen[$1]++' > $RES_FOLDER/dc-registered.csv

	echo "id,dc-started,man-adding,man-added,dc-registered" > $RES_FOLDER/join.csv
	join -t, \
		<(sort -t, -k 1 -n $RES_FOLDER/dc-started.csv) \
		<(sort -t, -k 1 -n $RES_FOLDER/man-adding.csv) \
		| join -t, - \
		<(sort -t, -k 1 -n $RES_FOLDER/man-added.csv) \
		| join -t, - \
		<(sort -t, -k 1 -n $RES_FOLDER/dc-registered.csv) \
		| sort -t, -k 2 - \
		>> $RES_FOLDER/join.csv
else

	for i in `seq 0 $(($nClients-1))`
	do
		echo "$i,$DC_KILLED_TS"
	done > $RES_FOLDER/dc-killed.csv

	grep -i "removing them" $RES_FOLDER/man.log \
		| sed 's/\([^ ]*\).*Resources \[\(.*\)\] expired.*/\2 \1/' \
		| \
		while read line
		do
			TS=${line##* }
			ids=$(echo $line | sed s/' [^ ]*$'/,/)
			for id in $ids
			do
				echo "$id$TS"
			done
		done | awk -F, '!seen[$1]++' > $RES_FOLDER/man-removing.csv

	grep -i "removed" $RES_FOLDER/man.log \
		| sed 's/\([^ ]*\).*Resources \[\(.*\)\] removed.*/\2 \1/' \
		| \
		while read line
		do
			TS=${line##* }
			ids=$(echo $line | sed s/' [^ ]*$'/,/)
			for id in $ids
			do
				echo "$id$TS"
			done
		done | awk -F, '!seen[$1]++' > $RES_FOLDER/man-removed.csv

	echo "id,dc-killed,man-removing,man-removed" > $RES_FOLDER/join.csv
	join -t, \
		<(sort -t, -k 1 -n $RES_FOLDER/dc-killed.csv) \
		<(sort -t, -k 1 -n $RES_FOLDER/man-removing.csv) \
		| join -t, - \
		<(sort -t, -k 1 -n $RES_FOLDER/man-removed.csv) \
		>> $RES_FOLDER/join.csv
fi
log "Done"