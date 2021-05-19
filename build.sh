#!/bin/bash
#
# Copyright (c) 2002-2021, Rice University.
# See the file edu.rice.cs.hpcviewer.ui/License.txt for details.
#
# Build hpcviewer and generate tar or zip files
# This script is only for Unix and X windows.
#

if ! command -v mvn &> /dev/null
then
	echo "Maven mvn command is not found"
	echo "Please install Apache maven"
	exit
fi

# 
# Update the release file 
#
GITC=`git rev-parse --short HEAD`
release=`date +"%Y.%m"`

echo "Release ${release}. Commit $GITC" > edu.rice.cs.hpcviewer.ui/release.txt
rm -rf hpcviewer-${release}*

#
# build the viewer
#
echo "=================================="
echo " Building the viewer"
echo "=================================="
mvn clean package

# The result should be:
#

echo "=================================="
echo " Repackaging the viewer"
echo "=================================="

# wrap the generated files into standard hpcviewer product
#

# repackage 
repackage_linux(){
	prefix=$1
	platform=$2
	package=`ls edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-${prefix}.${platform}*`
	extension="tgz"

	[[ -z $package ]] && { echo "$package doesn't exist"; exit 1;  }

	mkdir -p tmp/hpcviewer
	cd tmp/hpcviewer

	tar xzf  ../../$package
	cp ../../scripts/hpcviewer.sh .
	cp ../../scripts/install.sh .
	cp ../../scripts/README .

	cd ..

	output="hpcviewer-${release}-${prefix}.${platform}.$extension"
	tar czf ../$output hpcviewer/
	chmod 664 ../$output

	cd ..
	#ls -l $output
	rm -rf tmp
}


repackage_mac() {
      input=$1
      output=$2
      cp $input $output
      chmod 664 $output
}


repackage_windows() {
      input=$1
      output=$2
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

# The result should be:
#
# Building zip: edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-win32.win32.x86_64.zip
# Building zip: edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-macosx.cocoa.x86_64.zip
# Building tar: edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-linux.gtk.aarch64.tar.gz
# Building zip: edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-macosx.cocoa.x86_64.zip

repackage_linux linux.gtk x86_64
repackage_linux linux.gtk aarch64

# copy and rename windows package
output="hpcviewer-${release}-win32.win32.x86_64.zip"
input=edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-win32.win32.x86_64.zip
repackage_windows $input $output 

# copy and rename mac package
output="hpcviewer-${release}-macosx.cocoa.x86_64.zip"
input=edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-macosx.cocoa.x86_64.zip 
repackage_mac $input $output


###################################################################
# Special build for other than ppc64le mac and aarch64
###################################################################

cp releng/pom.xml releng/pom.4.18.xml
cp releng/pom.4.16.xml releng/pom.xml
mvn package

# result:
# Building tar: edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-linux.gtk.ppc64le.tar.gz

# repackage linux files
repackage_linux linux.gtk ppc64le

cp releng/pom.4.18.xml releng/pom.xml 

###################################################################
# special treatement for mac OS
###################################################################
OS=`uname`
if [[ "$OS" == "Darwin" && "$1" == "-n" ]]; then 
        echo "Notarize $output ..."
	macos/notarize.sh $output
	
fi


echo "=================================="
echo " Done" 
echo "=================================="

ls -l hpcviewer-${release}-*
