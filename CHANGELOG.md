---
currentMenu: changelog
---

# Change Log
All notable changes to this project will be documented in this file.
This document adheres to [keep-a-changelog].
This project adheres to [Semantic Versioning](http://semver.org/).

## [0.3.1] - 2015-09-15
### Added
- Support for cluster metrics by extending the model with new resources and relations (Node, Cluster, Rack).
- Flexiant data collector for monitoring cluster level metrics on their physical servers.

### Changed
- Output Metric action parameters `value` and `resourceId` are not mandatory anymore, see the doc to know the default values used.

## [0.3] - 2015-09-08
### Added
- CloudML action added to rules. It is now possible to scale up or down inside of the same cloud provider or bust to another one, in response to a condition on a monitored resource.

## [0.2.3] - 2015-07-29
### Fixed
- Problem with data collectors configuration when multiple rules with same input metrics were installed

## [0.2.2] - 2015-07-21
### Fixed
- Bug that caused two rules with the same collected metric to replace each other target resource

## [0.2.1] - 2015-06-17
### Added
- New Manager REST API and Manager API library for enabling and disabling rules
- New button on the manager webapp for enabling and disabling rules

### Fixed
- MODAClouds packaging

### Changed
- returned JSON from `/metrics/{metricname}/observers/` api changed to a simple list

## [0.2] - 2015-06-10
First official release of the new Tower 4Clouds platform.

## 0.1 - 2015-05-18
### Added
- First intial migration and integration of the old MODAClouds Monitoring Platform projects

[keep-a-changelog]: https://github.com/olivierlacan/keep-a-changelog
[0.3.1]: https://github.com/deib-polimi/tower4clouds/compare/v0.3...v0.3.1
[0.3]: https://github.com/deib-polimi/tower4clouds/compare/v0.2.3...v0.3
[0.2.3]: https://github.com/deib-polimi/tower4clouds/compare/v0.2.2...v0.2.3
[0.2.2]: https://github.com/deib-polimi/tower4clouds/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/deib-polimi/tower4clouds/compare/v0.2...v0.2.1
[0.2]: https://github.com/deib-polimi/tower4clouds/compare/v0.1...v0.2
