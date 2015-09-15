---
currentMenu: configuration
parentMenu: manager
---

#Configuration

## What to configure

* the Data Analyzer public endpoint IP address
* the Data Analyzer public endpoint port
* the Manager endpoint IP address
* the Manager endpoint port
* the RDF History DB IP address
* the RDF History DB Port

## How to configure

The monitoring manager can be configured by means of different options (latters replaces the formers):
* Default Configuration
* Environment Variables
* System Properties
* CLI Arguments

### Default Configuration

* the Data Analyzer public endpoint IP address: `127.0.0.1`
* the Data Analyzer public endpoint port: `8175`
* the Manager endpoint IP address: `127.0.0.1`
* the Manager endpoint port: `8170`
* the RDF History DB IP address: none
* the RDF History DB Port: `31337`

** If no RDF History DB IP address is specified, the manager assumes there is no RDF History DB and the model updates will not be saved on it **

### Environment Variables

* the Data Analyzer public endpoint IP address: `MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_ENDPOINT_IP_PUBLIC`
* the Data Analyzer public endpoint port: `MODACLOUDS_TOWER4CLOUDS_DATA_ANALYZER_ENDPOINT_PORT_PUBLIC`
* the Manager endpoint IP address: `MODACLOUDS_TOWER4CLOUDS_MANAGER_ENDPOINT_IP`
* the Manager endpoint port: `MODACLOUDS_TOWER4CLOUDS_MANAGER_ENDPOINT_PORT`
* the RDF History DB IP address: `MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_IP`
* the RDF History DB Port: `MODACLOUDS_TOWER4CLOUDS_RDF_HISTORY_DB_ENDPOINT_PORT`

### System Properties

Same values used for Environment Variables.

### CLI Arguments

Usage available by running `./tower4clouds-manager -h`:

```bash
Usage: tower4clouds-manager [options]
  Options:
    -h, --help
       Shows this message
       Default: false
    -v, --version
       Shows the version number
       Default: false
    -daip
       Data Analyzer public endpoint IP address
       Default: 127.0.0.1
    -daport
       Data Analyzer public endpoint port
       Default: 8175
    -manip
       Manager endpoint IP address
       Default: 127.0.0.1
    -manport
       Manager endpoint port
       Default: 8170
    -rdf-history-db-ip
       RDF History DB endpoint IP address
    -rdf-history-db-port
       RDF History DB endpoint port
       Default: 31337
```

