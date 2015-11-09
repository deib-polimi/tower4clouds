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


# Tested working on Amazon: Ubuntu Server 14.04 LTS (HVM), SSD Volume Type - ami-47a23a30

### available commands after installation:
# start-tower4clouds
# stop-tower4clouds
# cleanup-graphite
# cleanup-fuseki

export DEBIAN_FRONTEND=noninteractive
sudo apt-get -y -q update
sudo apt-get -y -q install default-jre

# tower4clouds
RELEASE=0.2.3
components=(data-analyzer manager-server rdf-history-db-main)
sudo mkdir /opt/tower4clouds
for component in ${components[@]}
do
	rm -f /tmp/${component}.tar.gz
    wget -O /tmp/${component}.tar.gz https://github.com/deib-polimi/tower4clouds/releases/download/v${RELEASE}/${component}-${RELEASE}.tar.gz
    sudo tar -zxf /tmp/${component}.tar.gz -C /opt/tower4clouds
done

cat > start-tower4clouds << EOF
RELEASE=${RELEASE}
components=(${components[@]})
startscripts=(tower4clouds-data-analyzer tower4clouds-manager tower4clouds-rdf-history-db)
NODE_PUBLIC_IP=\$(curl -s ipinfo.io/ip)
export \
    MODACLOUDS_RABBITMQ_ENDPOINT_IP=127.0.0.1 \
    MODACLOUDS_RABBITMQ_ENDPOINT_PORT=5672 \
    MODACLOUDS_FUSEKI_ENDPOINT_IP=127.0.0.1 \
    MODACLOUDS_FUSEKI_ENDPOINT_PORT=3030 \
    MODACLOUDS_FUSEKI_DB_PATH=/ds \
    MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_ENDPOINT_IP_PUBLIC=\${NODE_PUBLIC_IP} \
    MODACLOUDS_TOWER4CLOUDS_MANAGER_ENDPOINT_IP=\${NODE_PUBLIC_IP} \
    MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_ENDPOINT_PORT_PUBLIC=8175 \
    MODACLOUDS_TOWER4CLOUDS_MANAGER_ENDPOINT_PORT=8170 \
    MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_IP=127.0.0.1 \
    MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_PORT=31337
for (( i=0; i<\${#components[@]}; i++));
do
cd /opt/tower4clouds/\${components[i]}-\${RELEASE}/
nohup ./\${startscripts[i]} > /tmp/\${components[i]}-\${RELEASE}.log 2>&1 &
done
EOF
chmod +x start-tower4clouds
sudo mv start-tower4clouds /usr/bin

cat > stop-tower4clouds <<- EOF
pkill -f tower4clouds
EOF
chmod +x stop-tower4clouds
sudo mv stop-tower4clouds /usr/bin

export LANGUAGE="en_US.UTF-8"
export LC_ALL="en_US.UTF-8"

# grafana (default credentials: admin, admin)
wget --quiet https://grafanarel.s3.amazonaws.com/builds/grafana_2.0.2_amd64.deb
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install adduser libfontconfig
sudo dpkg -i grafana_2.0.2_amd64.deb
sudo service grafana-server start
sudo update-rc.d grafana-server defaults 95 10
rm grafana_2.0.2_amd64.deb

# graphite (https://www.digitalocean.com/community/tutorials/how-to-install-and-use-graphite-on-an-ubuntu-14-04-server)
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install graphite-web graphite-carbon
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install postgresql libpq-dev python-psycopg2

sudo pg_createcluster 9.3 main --start

cat > /tmp/init.sql <<- EOF
CREATE USER graphite WITH PASSWORD 'password';
CREATE DATABASE graphite WITH OWNER graphite;
EOF
sudo -u postgres psql --file=/tmp/init.sql
cat > /tmp/local_settings.py <<- EOF
SECRET_KEY = 'password'
TIME_ZONE = 'Europe/Rome'
LOG_RENDERING_PERFORMANCE = True
LOG_CACHE_PERFORMANCE = True
LOG_METRIC_ACCESS = True
GRAPHITE_ROOT = '/usr/share/graphite-web'
CONF_DIR = '/etc/graphite'
STORAGE_DIR = '/var/lib/graphite/whisper'
CONTENT_DIR = '/usr/share/graphite-web/static'
WHISPER_DIR = '/var/lib/graphite/whisper'
LOG_DIR = '/var/log/graphite'
INDEX_FILE = '/var/lib/graphite/search_index'
USE_REMOTE_USER_AUTHENTICATION = True
DATABASES = {
    'default': {
        'NAME': 'graphite',
        'ENGINE': 'django.db.backends.postgresql_psycopg2',
        'USER': 'graphite',
        'PASSWORD': 'password',
        'HOST': '127.0.0.1',
        'PORT': ''
    }
}
EOF
sudo mv /tmp/local_settings.py /etc/graphite/local_settings.py
sudo graphite-manage syncdb --noinput
cat > /tmp/graphite-carbon <<- EOF
CARBON_CACHE_ENABLED=true
EOF
sudo mv /tmp/graphite-carbon /etc/default/graphite-carbon
cat > /tmp/carbon.conf <<- EOF
[cache]
STORAGE_DIR    = /var/lib/graphite/
CONF_DIR       = /etc/carbon/
LOG_DIR        = /var/log/carbon/
PID_DIR        = /var/run/
LOCAL_DATA_DIR = /var/lib/graphite/whisper/
ENABLE_LOGROTATION = True
USER = _graphite
MAX_CACHE_SIZE = inf
MAX_UPDATES_PER_SECOND = 500
MAX_CREATES_PER_MINUTE = 50
LINE_RECEIVER_INTERFACE = 0.0.0.0
LINE_RECEIVER_PORT = 2003
ENABLE_UDP_LISTENER = False
UDP_RECEIVER_INTERFACE = 0.0.0.0
UDP_RECEIVER_PORT = 2003
PICKLE_RECEIVER_INTERFACE = 0.0.0.0
PICKLE_RECEIVER_PORT = 2004
LOG_LISTENER_CONNECTIONS = True
USE_INSECURE_UNPICKLER = False
CACHE_QUERY_INTERFACE = 0.0.0.0
CACHE_QUERY_PORT = 7002
USE_FLOW_CONTROL = True
LOG_UPDATES = False
LOG_CACHE_HITS = False
LOG_CACHE_QUEUE_SORTS = True
CACHE_WRITE_STRATEGY = sorted
WHISPER_AUTOFLUSH = False
WHISPER_FALLOCATE_CREATE = True

[relay]
LINE_RECEIVER_INTERFACE = 0.0.0.0
LINE_RECEIVER_PORT = 2013
PICKLE_RECEIVER_INTERFACE = 0.0.0.0
PICKLE_RECEIVER_PORT = 2014
LOG_LISTENER_CONNECTIONS = True
RELAY_METHOD = rules
REPLICATION_FACTOR = 1
DESTINATIONS = 127.0.0.1:2004
MAX_DATAPOINTS_PER_MESSAGE = 500
MAX_QUEUE_SIZE = 10000
USE_FLOW_CONTROL = True

[aggregator]
LINE_RECEIVER_INTERFACE = 0.0.0.0
LINE_RECEIVER_PORT = 2023
PICKLE_RECEIVER_INTERFACE = 0.0.0.0
PICKLE_RECEIVER_PORT = 2024
LOG_LISTENER_CONNECTIONS = True
FORWARD_ALL = True
DESTINATIONS = 127.0.0.1:2004
REPLICATION_FACTOR = 1
MAX_QUEUE_SIZE = 10000
USE_FLOW_CONTROL = True
MAX_DATAPOINTS_PER_MESSAGE = 500
MAX_AGGREGATION_INTERVALS = 5
EOF
sudo mv /tmp/carbon.conf /etc/carbon/carbon.conf
cat > /tmp/storage-schemas.conf <<- EOF
[carbon]
pattern = ^carbon\.
retentions = 60:90d

[default_1min_for_1day]
pattern = .*
retentions = 60s:1d
EOF
sudo mv /tmp/storage-schemas.conf /etc/carbon/storage-schemas.conf
cat > /tmp/storage-aggregation.conf <<- EOF
[min]
pattern = \.min$
xFilesFactor = 0.1
aggregationMethod = min

[max]
pattern = \.max$
xFilesFactor = 0.1
aggregationMethod = max

[sum]
pattern = \.count$
xFilesFactor = 0
aggregationMethod = sum

[default_average]
pattern = .*
xFilesFactor = 0.5
aggregationMethod = average
EOF
sudo mv /tmp/storage-aggregation.conf /etc/carbon/storage-aggregation.conf
sudo service carbon-cache start
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install apache2 libapache2-mod-wsgi
sudo a2dissite 000-default
sudo a2enmod headers
cat > /tmp/apache2-graphite.conf <<- EOF
<VirtualHost *:80>

	WSGIDaemonProcess _graphite processes=5 threads=5 display-name='%{GROUP}' inactivity-timeout=120 user=_graphite group=_graphite
	WSGIProcessGroup _graphite
	WSGIImportScript /usr/share/graphite-web/graphite.wsgi process-group=_graphite application-group=%{GLOBAL}
	WSGIScriptAlias / /usr/share/graphite-web/graphite.wsgi

	Alias /content/ /usr/share/graphite-web/static/
	<Location "/content/">
		SetHandler None
	</Location>

	ErrorLog ${APACHE_LOG_DIR}/graphite-web_error.log

	LogLevel warn

	CustomLog ${APACHE_LOG_DIR}/graphite-web_access.log combined
	Header set Access-Control-Allow-Origin "*"

</VirtualHost>
EOF
sudo mv /tmp/apache2-graphite.conf /etc/apache2/sites-available
sudo a2ensite apache2-graphite
sudo service apache2 reload
cat > cleanup-graphite << EOF
sudo rm -r /var/lib/graphite/whisper/*
EOF
chmod +x cleanup-graphite
sudo mv cleanup-graphite /usr/bin

#rabbitmq
sudo apt-get install -y -q rabbitmq-server

#fuseki
wget -O /tmp/jena-fuseki-1.1.1-distribution.tar.gz http://archive.apache.org/dist/jena/binaries/jena-fuseki-1.1.1-distribution.tar.gz
sudo tar zxf /tmp/jena-fuseki-1.1.1-distribution.tar.gz -C /opt
cd /opt/jena-fuseki-1.1.1
FUSEKI_HOME=`pwd`
sudo cp fuseki /etc/init.d/
sudo update-rc.d fuseki defaults
cd /tmp
cat > config.ttl << EOF
@prefix fuseki:  <http://jena.apache.org/fuseki#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix tdb:     <http://jena.hpl.hp.com/2008/tdb#> .
@prefix ja:      <http://jena.hpl.hp.com/2005/11/Assembler#> .
@prefix :        <#> .
[] rdf:type fuseki:Server ;
   fuseki:services (
     <#service_tdb_all>
   ) .
[] ja:loadClass "com.hp.hpl.jena.tdb.TDB" .
tdb:DatasetTDB  rdfs:subClassOf  ja:RDFDataset .
tdb:GraphTDB    rdfs:subClassOf  ja:Model .

<#service_tdb_all> rdf:type fuseki:Service ;
    rdfs:label                      "TDB Service (RW)" ;
    fuseki:name                     "ds" ;
    fuseki:serviceQuery             "query" ;
    fuseki:serviceQuery             "sparql" ;
    fuseki:serviceUpdate            "update" ;
    fuseki:serviceUpload            "upload" ;
    fuseki:serviceReadWriteGraphStore      "data" ;
    # A separate read-only graph store endpoint:
    fuseki:serviceReadGraphStore       "get" ;
    fuseki:dataset           <#tdb_dataset_readwrite> ;
    .

<#tdb_dataset_readwrite> rdf:type      tdb:DatasetTDB ;
    tdb:location "/tmp/fusekidb" ;
EOF
sudo mkdir /opt/jena-fuseki-1.1.1/configuration
sudo mv config.ttl /opt/jena-fuseki-1.1.1/configuration
printf "FUSEKI_HOME=$FUSEKI_HOME\nFUSEKI_CONF=/opt/jena-fuseki-1.1.1/configuration/config.ttl" > fuseki.parameters
sudo mv fuseki.parameters /etc/default/fuseki
sudo service fuseki start
cat > cleanup-fuseki << EOF
rm -r /tmp/fusekidb/*
EOF
chmod +x cleanup-fuseki
sudo mv cleanup-fuseki /usr/bin
