# Installation on mOSAIC

Requirements:
- mosaic-rt-jre-7

Installation:
- Define shared variables:
	```bash
	TOWER4CLOUDS_RELEASE=0.2
	```
- Download and install the rpms from the [latest release](https://github.com/deib-polimi/tower4clouds/releases) using zypper:
	```bash
	zypper \
			--non-interactive --no-refresh \
			--no-gpg-checks --gpg-auto-import-keys \
       	install \
			modaclouds-services-tower4clouds-rdf-history-db-${TOWER4CLOUDS_RELEASE}
	```

Start it:
- Define shared variables:
	```bash
	TOWER4CLOUDS_RELEASE=0.2
	```
- Start the [RDF History DB](rdf-history-db.md):
	```bash
	dtach -c /tmp/modaclouds-services-tower4clouds-rdf-history-db.dtach \
		/opt/modaclouds-services-tower4clouds-rdf-history-db-${TOWER4CLOUDS_RELEASE}/bin/service-run.bash
	```