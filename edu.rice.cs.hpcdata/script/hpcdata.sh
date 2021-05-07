#!/bin/sh
#
# Launch the hpcdata jar and set the variables.
#
# $Id $
#

# Mark's script to retrive the directory where the script runs
script="$0"
if test -L "$script" ; then
    script=`readlink "$script"`
fi
bindir=`dirname "$script"`

export HPCVIEWER_DIR_PATH="${bindir}/"

JAVA_CMD=java
MAIN_CLASS=edu.rice.cs.hpc.data.framework.PrintData
CLASSPATH=$HPCVIEWER_DIR_PATH/lib/commons-lang3-3.11.jar:$HPCVIEWER_DIR_PATH/lib/commons-text-1.9.jar:$HPCVIEWER_DIR_PATH/lib/hpcdata.jar
VM_ARGS="-Xms100m -Xmx2G"

# run the java main class
"$JAVA_CMD" $VM_ARGS -cp "$CLASSPATH" $MAIN_CLASS $*
