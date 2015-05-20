# Change Log
All notable changes to this project will be documented in this file.
This document adheres to [keep-a-changelog].
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased][unreleased]
[unreleased]: https://github.com/deib-polimi/tower4clouds/compare/v0.1...develop

** The RDF History DB has been moved from its [original repository](https://github.com/deib-polimi/modaclouds-history-db) to this one, and it has been integrated.** Changes in this section refer to changes with respect to the [last released version](https://github.com/deib-polimi/modaclouds-history-db/releases/tag/v0.1.4).

### Added
- If an RDF history db is configured, the manager is now sending updates about resources

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
_ In the RDF History DB the bugs given by the modifications in the used libraries are solved

## v0.1 - 2015-05-19

**This project was created by integrating several existing projects in a unique multi-module project in order to foster maintainability and exploitation of the Tower4Clouds platform.** Changes in this section refer to changes with respect to the last released version of each component, i.e.:
- https://github.com/deib-polimi/modaclouds-monitoring-manager/releases/tag/v1.7
- https://github.com/deib-polimi/modaclouds-qos-models/releases/tag/v2.4.4
- https://github.com/deib-polimi/modaclouds-app-level-dc/releases/tag/v0.6.3
- https://github.com/deib-polimi/modaclouds-data-collector-factory/releases/tag/v0.3.4
- https://github.com/deib-polimi/modaclouds-history-db/releases/tag/v0.1.4

** TODO **

[keep-a-changelog]: https://github.com/olivierlacan/keep-a-changelog
