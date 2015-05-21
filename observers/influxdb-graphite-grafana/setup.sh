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
# influxdb (default credentials: root, root)
wget --quiet http://s3.amazonaws.com/influxdb/influxdb_latest_amd64.deb
sudo dpkg -i influxdb_latest_amd64.deb
sudo /etc/init.d/influxdb start
# grafana (default credentials: admin, admin)
wget --quiet https://grafanarel.s3.amazonaws.com/builds/grafana_2.0.2_amd64.deb
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install adduser libfontconfig
sudo dpkg -i grafana_2.0.2_amd64.deb
sudo service grafana-server start
sudo update-rc.d grafana-server defaults 95 10
# graphite (https://www.digitalocean.com/community/tutorials/how-to-install-and-use-graphite-on-an-ubuntu-14-04-server)
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install graphite-web graphite-carbon
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install postgresql libpq-dev python-psycopg2
sudo -u postgres psql --file=/vagrant/graphite-conf/init.sql
sudo cp /vagrant/graphite-conf/local_settings.py /etc/graphite/local_settings.py
sudo graphite-manage syncdb --noinput
sudo cp /vagrant/graphite-conf/graphite-carbon /etc/default/graphite-carbon
sudo cp /vagrant/graphite-conf/carbon.conf /etc/carbon/carbon.conf
sudo cp /vagrant/graphite-conf/storage-schemas.conf /etc/carbon/storage-schemas.conf
sudo cp /vagrant/graphite-conf/storage-aggregation.conf /etc/carbon/storage-aggregation.conf
sudo service carbon-cache start
sudo DEBIAN_FRONTEND=noninteractive apt-get -y -q --force-yes install apache2 libapache2-mod-wsgi
sudo a2dissite 000-default
sudo a2enmod headers
sudo cp /vagrant/graphite-conf/apache2-graphite.conf /etc/apache2/sites-available
sudo a2ensite apache2-graphite
sudo service apache2 reload