#!/bin/bash

# libraries and jar needed to run hpcdata program
# if hpcdata requires more jar, need to include in this variable manually
# Notes: Please do not specify the version number of the jar file to keep portability
#	 across different versions

ROOT="../.."
TEMP="jars"
POSITIONAL=()

show_help(){
	echo "$0 [options] [commands]"
	echo "-h,--help          Show this help"
	echo "-r,--release <n>   Set the release number to <n>"
	echo "clean              Remove the temporary files"
	exit 0
}

clean_up() {
    	rm -rf $TEMP LICENSE
	exit 0
}

# default release number: yyyy.mm
RELEASE=`date +"%Y.%m"`

while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    clean)
	clean_up
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
	edu.rice.cs.hpcviewer.product/target/repository/plugins/org.eclipse.collections*.jar \
	externals/graphbuilder/target/com.graphbuilder-*.jar \
	edu.rice.cs.hpcdata/target/edu.rice.cs.hpcdata-*.jar \
	edu.rice.cs.hpcdata.merge/target/edu.rice.cs.hpcdata.merge-*.jar \
	edu.rice.cs.hpcdata.app/target/edu.rice.cs.hpcdata.app-*.jar"

rm -rf ${TEMP}
mkdir ${TEMP}

for f in ${FILES}; do
	JAR="${ROOT}/${f}"
	if [ -r ${JAR} ]; then 
		BASE=`basename $JAR`
		CMD="cp $JAR ${TEMP}/${BASE}"
		echo $CMD
		${CMD}
	else
		echo "File not found: ${JAR}"
		exit 1
	fi
done
echo "tar the script and jar files..."
cp ../../LICENSE .
tar czf hpcdata-${RELEASE}.tgz  hpcdata.sh ${TEMP} LICENSE
