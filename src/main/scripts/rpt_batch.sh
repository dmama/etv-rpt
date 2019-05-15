#!/usr/bin/env bash

SCRIPT_PATH=$(dirname `which $0`)
source $SCRIPT_PATH"/envsBatch.sh"

echo " envBatch vm_options: ${vm_options}"

if [ -z "$JAVA_HOME" ]; then
	echo "Environment variable JAVA_HOME must be set!" >&2
	exit 1
elif [ ! -e "$JAVA_HOME/bin/java" ]; then
	echo "File $JAVA_HOME/bin/java cannot be found!" >&2
	exit 1
fi

$JAVA_HOME/bin/java ${vm_options}  -jar  ../unireg-rpt-*.jar ${batch_params}
