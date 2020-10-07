#!/bin/bash

GITC=`git rev-parse --short HEAD`
DATE=`date +"%d.%m.%Y"`

echo "Release ${DATE}. Commit $GITC" > edu.rice.cs.hpcviewer.ui/release.txt

mvn clean package
platform=`uname -m`
package="edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-$platform*"
echo "package: $package"

if [ -e "$package" ]; then
	echo "found: $package"
fi
