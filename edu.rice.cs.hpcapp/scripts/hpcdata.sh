#!/bin/bash

JAR_DIR="jars/*.jar"
MAIN_CLASS="edu.rice.cs.hpcapp.PrintData"
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
