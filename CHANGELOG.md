# Change Log
All notable changes to this project will be documented in this file.
This document adheres to [keep-a-changelog].
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased][unreleased]
[unreleased]: https://github.com/deib-polimi/tower4clouds/compare/v0.1...develop

### Added
- If an RDF history db is configured, the manager is now sending updates about resources
- The RDF History DB has been moved from its [original repository](https://github.com/deib-polimi/modaclouds-history-db) to this one, and it has been integrated with the other subprojects

### Changed
- Data collector configuration is now deleted from the ServerFacade local memory when the server is down
- Improved logging information

### Fixed
- Jena-core and knowledge base api version changed for a conflict which prevented the platform to work
- DefaultRestClient NullPointerException problem fixed
- Webapp rest call to model is updated to the new version, and the model loads fine now
- Fixed bug that was causing null pointer exception when loading an empty list of observers
- Fixed bug that did not allow to attach observers through the web control panel


[keep-a-changelog]: https://github.com/olivierlacan/keep-a-changelog
