#!/bin/bash

## chunk::dd0589220f7c9bd3c2b62f4e1edc9b95::begin ##
set -e -E -u -o pipefail -o noclobber -o noglob +o braceexpand || exit 1
trap 'printf "[ee] failed: %s\n" "${BASH_COMMAND}" >&2' ERR || exit 1

if test "$( getent passwd -- mos-services | cut -f 3 -d : )" -ne "${UID}" ; then
	exec sudo -u mos-services -g mos-services -E -n -- "${0}" "${@}"
	exit 1
fi

if ! test "${#}" -eq 0 ; then
	printf '[ii] invalid arguments; aborting!\n' >&2
	exit 1
fi

umask 0027

exec </dev/null >&2
## chunk::dd0589220f7c9bd3c2b62f4e1edc9b95::end ##

_variable_defaults=(
		
		_FUSEKI_HOME='@{definitions:environment:FUSEKI_HOME}'
		_FUSEKI_VAR='@{definitions:environment:FUSEKI_VAR}'
		_FUSEKI_ENDPOINT_IP='@{definitions:environment:FUSEKI_ENDPOINT_IP}'
		_FUSEKI_ENDPOINT_PORT='@{definitions:environment:FUSEKI_ENDPOINT_PORT}'
		_FUSEKI_DATASET_PATH='@{definitions:environment:FUSEKI_DATASET_PATH}'
		
		_JAVA_HOME='@{definitions:environment:JAVA_HOME}'
		_PATH='@{definitions:environment:PATH}'
		_TMPDIR='@{definitions:environment:TMPDIR}'
)
declare "${_variable_defaults[@]}"

## chunk::fdccb2b60ff605167433bb1c89bd0b84::begin ##
export PATH="${_PATH}"

_identifier="${modaclouds_service_identifier:-0000000000000000000000000000000000000000}"

if test -n "${modaclouds_service_temporary:-}" ; then
	_TMPDIR="${modaclouds_service_temporary:-}"
elif test -n "${modaclouds_temporary:-}" ; then
	_TMPDIR="${modaclouds_temporary}/services/${_identifier}"
else
	_TMPDIR="${_TMPDIR}/${_identifier}"
fi
## chunk::fdccb2b60ff605167433bb1c89bd0b84::end ##

_variable_overrides=(
		
		_FUSEKI_VAR="${MODACLOUDS_FUSEKI_VAR:-${_FUSEKI_VAR}}"
		_FUSEKI_ENDPOINT_IP="${MODACLOUDS_ENDPOINT_IP:-${_FUSEKI_ENDPOINT_IP}}"
		_FUSEKI_ENDPOINT_PORT="${MODACLOUDS_ENDPOINT_PORT:-${_FUSEKI_ENDPOINT_PORT}}"
		_FUSEKI_DATASET_PATH="${MODACLOUDS_DATASET_PATH:-${_FUSEKI_DATASET_PATH}}"
		
		_TMPDIR="${MODACLOUDS_FUSEKI_TMPDIR:-${_TMPDIR}}"
)
declare "${_variable_overrides[@]}"

## chunk::3a0efc2555cc97891ac1266f5065920a::begin ##
if test ! -e "${_TMPDIR}" ; then
	mkdir -p -- "${_TMPDIR}"
	mkdir -- "${_TMPDIR}/tmp"
	mkdir -- "${_TMPDIR}/home"
fi

exec {_lock}<"${_TMPDIR}"
if ! flock -x -n "${_lock}" ; then
	printf '[ee] failed to acquire lock; aborting!\n' >&2
	exit 1
fi

if test -d "${_TMPDIR}/cwd" ; then
	chmod -R u+w -- "${_TMPDIR}/cwd"
	rm -R -- "${_TMPDIR}/cwd"
fi
mkdir -- "${_TMPDIR}/cwd"

cd -- "${_TMPDIR}/cwd"

_environment=(
		PATH="${_PATH}"
		TMPDIR="${_TMPDIR}/tmp"
		HOME="${_TMPDIR}/home"
		USER='modaclouds-services'
)
## chunk::3a0efc2555cc97891ac1266f5065920a::end ##

if test ! -e "${_FUSEKI_VAR}" ; then
	_FUSEKI_VAR="${_TMPDIR}/var"
fi
if test ! -e "${_FUSEKI_VAR}" ; then
	mkdir -- "${_FUSEKI_VAR}"
fi

printf '[--]\n' >&2
printf '[ii] parameters:\n' >&2
printf '[ii]   * fuseki endpoint: `http://%s:%s/`;\n' "${_FUSEKI_ENDPOINT_IP}" "${_FUSEKI_ENDPOINT_PORT}" >&2
printf '[ii]   * fuseki dataset: `%s`;\n' "${_FUSEKI_DATASET_PATH}" >&2
printf '[ii]   * fuseki installation: `%s`;\n' "${_FUSEKI_HOME}" >&2
printf '[ii]   * fuseki database: `%s`;\n' "${_FUSEKI_VAR}" >&2
printf '[ii]   * environment:\n' >&2
for _variable in "${_environment[@]}" ; do
	printf '[ii]       * `%s`;\n' "${_variable}" >&2
done
printf '[ii]   * workding directory: `%s`;\n' "${PWD}" >&2
printf '[--]\n' >&2

printf '[ii] starting Fuseki...\n' >&2
printf '[--]\n' >&2

exec \
	env \
			-i "${_environment[@]}" \
	"${_JAVA_HOME}/bin/java" \
			-jar "${_FUSEKI_HOME}/fuseki-server.jar" \
			--home="${_FUSEKI_HOME}" \
			--port="${_FUSEKI_ENDPOINT_PORT}" \
			--loc="${_FUSEKI_VAR}" \
			--update \
			-- \
			"${_FUSEKI_DATASET_PATH}"

exit 1