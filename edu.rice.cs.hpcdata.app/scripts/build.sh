#!/bin/bash

# SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
#
# SPDX-License-Identifier: BSD-3-Clause

# libraries and jar needed to run hpcdata program
# if hpcdata requires more jar, need to include in this variable manually
# Notes: Please do not specify the version number of the jar file to keep portability
#	 across different versions

ROOT="../.."
TEMP="jars"
POSITIONAL=()

# default release number: yyyy.mm
RELEASE=`date +"%Y.%m"`
VERBOSE=0

show_version() {
	echo "hpcdata $RELEASE"
}

show_help(){
	echo "$0 [options] [commands]"
	echo "-h,--help          Show this help"
	echo "-r,--release <n>   Set the release number to <n>"
	echo "clean              Remove the temporary files"
	echo "distclean          Remove the temporary and target files"
	exit 0
}

clean_up() {
    	rm -rf $TEMP LICENSE
}

distclean_up() {
	rm -f hpcdata-*
	clean_up
}

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    clean)
	clean_up
	exit 0
    	;;
    distclean)
	distclean_up
	exit 0
    	;;
    -v|--verbose)
	VERBOSE=1
	shift
	;;
    -h|--help)
	show_help
	shift
	;;
    -r|--release)
     	RELEASE="$2"
	shift
	shift
	;;
esac
done

set -- "${POSITIONAL[@]}" # restore positional parameters

FILES="edu.rice.cs.hpcviewer.product/target/repository/plugins/ca.odell.glazedlists*.jar \
	edu.rice.cs.hpcviewer.product/target/repository/plugins/org.yaml.snakeyaml*.jar \
	edu.rice.cs.hpcviewer.product/target/repository/plugins/org.apache.commons.math3*.jar \
	edu.rice.cs.hpcviewer.product/target/repository/plugins/org.eclipse.collections*.jar \
	externals/com.graphbuilder/graphbuilder-*.jar \
	edu.rice.cs.hpcdata/hpcdata-*.jar \
	edu.rice.cs.hpcdata.merge/target/edu.rice.cs.hpcdata.merge-*.jar \
	edu.rice.cs.hpcdata.app/target/edu.rice.cs.hpcdata.app-*.jar"

rm -rf ${TEMP}
mkdir ${TEMP}

for f in ${FILES}; do
	JAR="${ROOT}/${f}"
	if [ -r ${JAR} ]; then 
		BASE=`basename $JAR`
		CMD="cp $JAR ${TEMP}/${BASE}"
		if [ $VERBOSE == "1" ]; then
			echo $CMD
		fi
		${CMD}
	else
		echo "File not found: ${JAR}"
		exit 1
	fi
done
if [[ "$VERBOSE" == "1" ]]; then
	echo "tar the script and jar files..."
fi
cp ../../LICENSE .
tar czf hpcdata.tgz  hpcdata.sh ${TEMP} LICENSE
