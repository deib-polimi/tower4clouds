# Change Log
All notable changes to this project will be documented in this file.
This document adheres to [keep-a-changelog].
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased][unreleased]
[unreleased]: https://github.com/deib-polimi/tower4clouds/compare/v0.1...develop

### Note
The RDF History DB has been moved from its [original repository](https://github.com/deib-polimi/modaclouds-history-db) to this one, and it has been integrated. Changes in this section refer to changes with respect to the [latest released version](https://github.com/deib-polimi/modaclouds-history-db/releases/tag/v0.1.4) of the component.

### Added
- If an RDF history db is configured, the manager is now sending updates about resources
- Possibility to specify protocol, format, observer host and port when attaching an observer to a metric from the manager web control panel
- Vagrantfile in the observers folder 'influxdb-graphite-grafana' for creating a VM with graphite, influxdb and grafana installed and running
- Vagrantfile in the observers module 'rdf-history-db' for creating a VM with RabbitMQ and Fuseki
- Traffic Generator for testing and showcasing purposes in MockApplication module
- Data2Stdout, a simple observer application that prints data to standard output
- Useful scripts in folder scripts for starting and stopping the entire platform and one for destroying vms
- java app dc implemented

### Changed
- Data collector configuration is now deleted from the ServerFacade local memory when the server is down
- Improved logging information
- The RDF History DB Rest API for the resources is now `/resources`

### Fixed
- Jena-core and knowledge base api version changed for a conflict which prevented the platform to work
- DefaultRestClient NullPointerException problem fixed
- Webapp rest call to model is updated to the new version, and the model loads fine now
- Fixed bug that was causing null pointer exception when loading an empty list of observers
- Fixed bug that did not allow to attach observers through the Manager web control panel
- In the RDF History DB the bugs given by the modifications in the used libraries are solved
- Fixed the create queue subscribe to it race in the RDF History DB
- Fixed bug in the Manager that caused an observer to be deleted from registry when deleted from a metric and was also observing another metric

## v0.1 - 2015-05-19

**This project was created by integrating several existing projects in a unique multi-module project in order to foster maintainability and exploitation of the Tower4Clouds platform.** Changes in this section refer to changes with respect to the last released version of each component, i.e.:
- https://github.com/deib-polimi/modaclouds-monitoring-manager/releases/tag/v1.7
- https://github.com/deib-polimi/modaclouds-qos-models/releases/tag/v2.4.4
- https://github.com/deib-polimi/modaclouds-app-level-dc/releases/tag/v0.6.3
- https://github.com/deib-polimi/modaclouds-data-collector-factory/releases/tag/v0.3.4
- https://github.com/deib-polimi/modaclouds-history-db/releases/tag/v0.1.4

** TODO **

[keep-a-changelog]: https://github.com/olivierlacan/keep-a-changelog
