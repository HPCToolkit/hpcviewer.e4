#!/bin/bash

# Copyright (c) 2002-2024, Rice University.
# SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
#
# SPDX-License-Identifier: BSD-3-Clause

#
# Build hpcviewer and generate tar or zip files
# This script is only for Unix and X windows.
#

#------------------------------------------------------------
# Error messages
#------------------------------------------------------------
die()
{
    echo "$0: error: $*" 1>&2
    exit 1
}


#------------------------------------------------------------
# Check maven existence: exit if it doesn't exist
#------------------------------------------------------------
check_maven()
{
    if ! command -v mvn &> /dev/null
    then
        die "Apache Maven (mvn) command is not found"
    fi
}


#------------------------------------------------------------
# Check the java version.
#------------------------------------------------------------
check_java()
{
	JAVA=`type -p java`
	
	if [ "$JAVA"x != "x" ]; then
	    JAVA=java
	elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
	    echo found java executable in JAVA_HOME     
	    JAVA="$JAVA_HOME/bin/java"
	else
	    die "unable to find program 'java' on your PATH"
	fi
	
	JAVA_MAJOR_VERSION=`java -version 2>&1 \
		  | head -1 \
		  | cut -d'"' -f2 \
		  | sed 's/^1\.//' \
		  | cut -d'.' -f1`
	
	echo "Java version $JAVA_MAJOR_VERSION"
	
	# we need Java 17 at least
	jvm_min=17
	jvm_max=22

	# issue #308: we don't support too new Java
	if [ "$JAVA_MAJOR_VERSION" -lt "$jvm_min" ] || [ "$JAVA_MAJOR_VERSION" -gt "$jvm_max" ]; then
		die "$name requires Java between $jvm_min and $jvm_max"
	fi
}


#------------------------------------------------------------
# Show all the options and then exit
#------------------------------------------------------------
show_help(){
	echo "Syntax: $0 [-options] [commands]"
	echo "Options: "
	echo " -c              	create a package and generate the release number"
	echo " -n              	(Mac only) notarize the package"
	echo " -i              	(Mac only) create a disk image file"
	echo " -r <release>    	specify the release number"
	echo "Commands:"
	echo " check            build and run the tests."
	echo " clean            remove objects and temporary files"
	echo " distclean        remove the temporary and target files"
	exit
}


#------------------------------------------------------------
# remove all the temporary files
#------------------------------------------------------------
clean_up() {
	./mvnw clean
	rm -f scripts/hpcviewer_launcher.sh
}


#------------------------------------------------------------
# remove all the temporary AND product files 
#------------------------------------------------------------
distclean_up() {
	clean_up
	rm -rf hpcviewer-* hpcdata-* prepare_sign*
}


#------------------------------------------------------------
# Generate a hpcviewer for Linux package inside
# `edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-${prefix}.${platform}*``
# @param prefix 
# @param machine_platform 
#------------------------------------------------------------
repackage_linux(){
	prefix=$1
	platform=$2
	package=`ls edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-${prefix}.${platform}*`
	extension="tgz"

	[[ -z $package ]] && { echo "$package doesn't exist"; exit 1;  }

	mkdir -p tmp/hpcviewer
	cd tmp/hpcviewer

	#tar xzf  ../../$package | grep -v 'LIBARCHIVE.creationtime'
	unzip -qq ../../$package

	cp ../../$launcher .
	cp ../../scripts/install.sh .
	cp ../../scripts/README .

	cd ..

	output="hpcviewer-${prefix}.${platform}.$extension"
	if [[ "$VERBOSE" == 1 ]]; then
		echo "Packaging $output from $package"
	fi
	tar czf ../$output hpcviewer/
	chmod 664 ../$output

	cd ..
	#ls -l $output
	rm -rf tmp
}


#------------------------------------------------------------
# Generate a hpcviewer package for mac 
#------------------------------------------------------------
repackage_mac() {
      input=$1
      output=$2
      if [[ "$VERBOSE" == 1 ]]; then
	      echo "Packaging $output from $input"
      fi
      cp $input $output
      chmod 664 $output
}


#------------------------------------------------------------
# Generate a hpcviewer package for Windows
#------------------------------------------------------------
repackage_windows() {
      input=$1
      output=$2
      if [[ "$VERBOSE" == 1 ]]; then
		echo "Packaging $output from $input"
      fi
      
      # for windows, we need to create a special hpcviewer directory
      if [ -e hpcviewer ]; then
         echo "File or directory hpcviewer already exist. Do you want to remove it? (y/n) "
         read tobecontinue
         if [ $tobecontinue != "y" ]; then
	         exit
	     fi
      fi
      rm -rf hpcviewer
      mkdir hpcviewer
      cd hpcviewer
      unzip -q ../$input
      cd ..
      zip -q -r -y $output hpcviewer/
      rm -rf hpcviewer
      
      chmod 664 $output
}



#------------------------------------------------------------
# generate HTML documents and make a package of them 
#------------------------------------------------------------
generate_documents()
{
    cd doc
    ../mvnw site
    if [ ! -f target/site/index.html ]; then
        die "Unable to generate the documentation"
    fi
    
    tar cf manual.tar target/site/*
    cd ..
}


###################################################################
# packaging the hpcdata
###################################################################

#
# checking dependencies
#
check_maven
check_java

#
# parse the arguments
#
CHECK_PACKAGE=0
CREATE_PACKAGE=1
VERBOSE=0
NOTARIZE=0
RELEASE=`date +"%Y.%m"`
IMAGE_ONLY=0

POSITIONAL=()
while [[ $# -gt 0 ]]
do
	key="$1"
	
	case $key in
	    check)
	    CHECK_PACKAGE=1
	    shift # past argument
	    ;;
	    clean)
	    clean_up
	    shift # past argument
	    exit
	    ;;
	    distclean)
	    distclean_up
	    shift # past argument
	    exit
	    ;;
	
	    -c|--create)
	    CREATE_PACKAGE=0
	    shift # past argument
	    ;;
	    -h|--help)
	    show_help
	    shift # past argument
	    ;;
	    -i|--image)
	    IMAGE_ONLY=1
	    shift # past argument
	    ;;
	    -n|--notarize)
	    NOTARIZE=1
	    shift # past argument
	    ;;
	    -r|--release)
	    RELEASE="$2"
	    CREATE_PACKAGE=0
	    shift # past argument
	    shift # past value
	    ;;
	    -v|--verbose)
	    VERBOSE=1
	    shift # past argument
	    ;;
	    --default)
	    DEFAULT=YES
	    shift # past argument
	    ;;
	    *)    # unknown option
	    POSITIONAL+=("$1") # save it in an array for later
	    shift # past argument
	    ;;
	esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

# 
# Update the release file 
#
GITC=`git rev-parse --short HEAD`
name="hpcviewer"
sh_file="scripts/${name}.sh"
launcher_name="${name}_launcher.sh"
launcher="scripts/${launcher_name}"
RELEASE_FILE="edu.rice.cs.hpcviewer.ui/release.txt"
OS=`uname`

# insert the release number to the launcher script:
# first, copy the launcher script
# second, replace the release variable with the current version

cp -f "$sh_file" "$launcher" \
       || die "unable to copy files"

if [ "$CREATE_PACKAGE" == "0" ]; then
    # create the release number: the current month and the commit hash
    # users may don't care with the hash, but it's important for debugging
    VERSION="Release ${RELEASE}. Commit $GITC" 
    echo "$VERSION" > ${RELEASE_FILE}
else
    VERSION=`cat $RELEASE_FILE`   	    
fi

if [[ "$OS" == "Darwin" ]]; then 
    sed -i '' "s/__VERSION__/\"$VERSION\"/g" $launcher
else
    sed -i "s/__VERSION__/\"$VERSION\"/g" $launcher
fi


#
# build the viewer
#
echo "=================================="
echo " Building the viewer"
echo "=================================="
if [ $CHECK_PACKAGE != "0" ]; then
	./mvnw clean verify -Pjacoco
	if [ -d tests/edu.rice.cs.hpctest.report/target/site/jacoco-aggregate/ ]; then
		echo "Code coverage result: tests/edu.rice.cs.hpctest.report/target/site/jacoco-aggregate/"
	fi
else
	./mvnw clean package
fi

# The result should be:
#   	edu.rice.cs.hpcviewer.product/target/products/*
if [ ! -e "edu.rice.cs.hpcviewer.product/target/products" ]; then
	die "Fail to build hpcviewer"
fi


#
# Generate the user manual
#
generate_documents

echo "=================================="
echo " Repackaging the viewer"
echo "=================================="

#
# wrap the generated files into standard hpcviewer product
#

repackage_linux linux.gtk x86_64
repackage_linux linux.gtk aarch64
repackage_linux linux.gtk ppc64le 

# copy and rename windows package
output="hpcviewer-win32.win32.x86_64.zip"
input=edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-win32.win32.x86_64.zip
repackage_windows $input $output 

# copy and rename mac x86_64 package
output="hpcviewer-macosx.cocoa.x86_64.zip"
input=edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-macosx.cocoa.x86_64.zip 
repackage_mac $input $output

# copy and rename mac aarch64 package
output="hpcviewer-macosx.cocoa.aarch64.zip"
input=edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-macosx.cocoa.aarch64.zip 
repackage_mac $input $output

# special treatment for mac OS to notarize if needed
if [[ "$OS" == "Darwin" && "$NOTARIZE" == "1" ]]; then 
    macPkgs="hpcviewer-macosx.cocoa.x86_64.zip"
	macos/notarize.sh $macPkgs
	
        macPkgs="hpcviewer-macosx.cocoa.aarch64.zip"
	macos/notarize.sh $macPkgs

elif [[ "$OS" == "Darwin" && "$IMAGE_ONLY" == "1" ]]; then 
        macPkgs="hpcviewer-macosx.cocoa.aarch64.zip"
	macos/notarize.sh -i $macPkgs
fi



###################################################################
# End
###################################################################

echo "=================================="
echo " Done" 
echo "=================================="

ls -l hpcviewer-*
if [  -f hpcdata.tgz ]; then
	ls -l hpcdata.tgz
fi
