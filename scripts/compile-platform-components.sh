#!/bin/bash 
function log {
	printf "\033[1;36m$1\033[0m\n"
}

cd ..
log 'Compiling and packaging the project'
mvn clean package