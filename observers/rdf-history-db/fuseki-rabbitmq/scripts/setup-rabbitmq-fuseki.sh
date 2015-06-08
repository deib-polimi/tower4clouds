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

export DEBIAN_FRONTEND=noninteractive
sudo apt-get -y -q update
sudo apt-get -f -y -q install
sudo apt-get -y -q install default-jre

echo 'Installing and starting RabbitMQ'
sudo apt-get install -y -q rabbitmq-server

echo 'Downloading Fuseki'
wget --quiet http://archive.apache.org/dist/jena/binaries/jena-fuseki-1.1.1-distribution.tar.gz
echo 'Installing and starting Fuseki'
tar zxvf jena-fuseki-1.1.1-distribution.tar.gz
cd jena-fuseki-1.1.1
FUSEKI_HOME=`pwd`
sudo cp fuseki /etc/init.d/
sudo update-rc.d fuseki defaults
cd ..
printf "FUSEKI_HOME=$FUSEKI_HOME" > fuseki.parameters
sudo mv fuseki.parameters /etc/default/fuseki
sudo service fuseki start