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

pkill java

if [[ $# -lt 2 ]] ; then
    echo 'Missing arguments [processes] [threads].'
    exit 1
fi

SCRIPT_PATH=`pwd`

# PIPE_FILE=/tmp/pipe_tower_evaluation
# if [ ! -e $PIPE_FILE ]
# then
#   mkfifo $PIPE_FILE
# fi

nClients=$(($1*$2))
echo "Number of clients = ${nClients}"
RES_FOLDER=${SCRIPT_PATH}/results/$nClients
rm -rf $RES_FOLDER
mkdir -p $RES_FOLDER

# tail -f $PIPE_FILE > $RES_FOLDER/dc.log &
# KILL=$!
# echo "" > /tmp/pipe_tower_evaluation

java -jar ../../../data-analyzer/target/data-analyzer-*-jar-with-dependencies.jar 2> $RES_FOLDER/da.log &
KILL="${!}"
java -jar ../../../manager/manager-server/target/manager-server-*-jar-with-dependencies.jar 2> $RES_FOLDER/man.log &
KILL="${!} ${KILL}"

sleep 10

i="0"
while [ $i -lt $1 ]
do
	java -jar ../../target/evaluation-*-jar-with-dependencies.jar -process $i -threads $2 2> $RES_FOLDER/dc_${i}.log &
	KILL="${!} ${KILL}"
	i=$[$i+1]
done

sleep 60

kill -SIGINT $KILL

cat $RES_FOLDER/dc_*.log > $RES_FOLDER/dc.log
rm $RES_FOLDER/dc_*.log
cat $RES_FOLDER/dc.log | grep started | awk -F' ' '{print $8","$1}' > $RES_FOLDER/dc-started.csv
cat $RES_FOLDER/man.log | grep added | sed 's/\([^ ]*\).*id=\(.*\)]].*/\2,\1/' > $RES_FOLDER/dc-registered.csv

join -t, <(sort -t, -k 1 -n $RES_FOLDER/dc-started.csv) <(sort -t, -k 1 -n $RES_FOLDER/dc-registered.csv) > $RES_FOLDER/join.csv