#!/bin/bash

GITC=`git rev-parse --short HEAD`
release=`date +"%Y.%m.%d"`

echo "Release ${release}. Commit $GITC" > edu.rice.cs.hpcviewer.ui/release.txt

mvn clean package
platform=`uname -m`
prefix="linux.gtk"
extension="tgz"

os=`uname`

if [ "$os" == "Darwin" ]; then
  prefix="macosx.cocoa"
  extension="zip"
fi

package=`ls edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-${prefix}.${platform}*`

if [ -e "$package" ]; then
  echo "package: $package"
else
  echo "package ${package} not found"
  exit
fi

mkdir -p tmp/hpcviewer
cd tmp/hpcviewer

tar xzf  ../../$package
cp ../../scripts/* .

cd ..

output="hpcviewer-${release}-${prefix}.${platform}.$extension"

tar czf ../$output hpcviewer/

cd ..

ls -l $output

rm -rf tmp
