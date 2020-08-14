#!/bin/bash

mvn clean package
platform=`uname -m`
package="edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-$platform*"
echo "package: $package"

if [ -e "$package" ]; then
	echo "found: $package"
fi
