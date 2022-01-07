#!/bin/bash

# libraries and jar needed to run hpcapp program
# if hpcdata requires more jar, need to include in this variable manually
# Notes: Please do not specify the version number of the jar file to keep portability
#	 across different versions

ROOT="../.."
TEMP="jars"
POSITIONAL=()

show_help(){
	echo "$0 [options] [commands]"
	echo "-h,--help   show this help"
	echo "clean       remove the temporary files"
	exit 0
}

clean_up() {
    	rm -rf $TEMP
	exit 0
}

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
esac
done

set -- "${POSITIONAL[@]}" # restore positional parameters

FILES="edu.rice.cs.hpcviewer.product/target/repository/plugins/ca.odell.glazedlists*.jar \
	edu.rice.cs.hpcviewer.product/target/repository/plugins/org.eclipse.collections*.jar \
	externals/graphbuilder/target/com.graphbuilder-*.jar \
	edu.rice.cs.hpcdata/target/edu.rice.cs.hpcdata-*.jar \
	edu.rice.cs.hpcdata.merge/target/edu.rice.cs.hpcdata.merge-*.jar \
	edu.rice.cs.hpcapp/target/edu.rice.cs.hpcapp-*.jar"

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
tar czf hpcdata.tgz  hpcdata.sh ${TEMP}
echo "output: "
ls -l *.tgz
