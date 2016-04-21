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

if [[ $# -lt 3 ]] ; then
    echo "Usage: $0 server-address server-user server-ssh-key"
    exit 1
fi

ADDR=$1
USER=$2
SSH_KEY=$3

if [ -z $ADDR ]; then
	log "No server address specified"
	exit 1
else
	ping -c 1 $ADDR > /dev/null
	if [ ! $? = 0 ]; then
		log "Ping failed for host ${ADDR}"
		exit 1
	fi
	if [ -z $USER ]; then
		log "No user specified"
		exit 1
	fi
	if [ -z "$SSH_KEY" ]; then
		log "No ssh key specified"
		exit 1
	fi
	ssh -q -i "$SSH_KEY" ${USER}@${ADDR} exit
	if [ ! $? = 0 ]; then
		log "Cannot ssh to ${ADDR}"
		exit 1
	fi
fi

scp -i "${SSH_KEY}" \
	../../manager/manager-server/target/manager-server-0.4-SNAPSHOT-jar-with-dependencies.jar \
	${USER}@${ADDR}:/usr/local/manager-server-0.3.1/tower4clouds-manager.jar
scp -i "${SSH_KEY}" \
	../../data-analyzer/target/data-analyzer-0.4-SNAPSHOT-jar-with-dependencies.jar \
	${USER}@${ADDR}:/usr/local/data-analyzer-0.3.1/tower4clouds-data-analyzer.jar