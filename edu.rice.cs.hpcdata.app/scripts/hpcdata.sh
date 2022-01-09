#!/bin/bash

DIR=`dirname $(readlink -f $0)`
JAR_DIR="${DIR}/jars/*.jar"
MAIN_CLASS="edu.rice.cs.hpcdata.app.PrintData"
JAR_FILES=""

for j in $JAR_DIR; do
	if [ "x$JAR_FILES" == "x" ]; then
		JAR_FILES="$j"
	else
		JAR_FILES="${JAR_FILES}:$j"
	fi
done

CMD="java -cp ${JAR_FILES} ${MAIN_CLASS}"
#echo "$CMD $@"
$CMD $@
