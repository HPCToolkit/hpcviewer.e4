#/bin/bash

release=`date +"%Y.%m.%d"`

dir="/tmp/hpcvtest-$release"
rm -rf $dir
mkdir -p $dir
cd $dir

git clone https://github.com/hpctoolkit/hpcviewer.e4

[[ -z hpcviewer.e4 ]] && { echo "hpcviewer.e4 doesn't exist"; exit 1;  }

cd hpcviewer.e4
./build.sh 2> log.err

platform=`uname -m`
prefix="linux.gtk"
extension="tgz"

output="hpcviewer-${release}-${prefix}.${platform}.$extension"

if [ -e $output ]; then
	tmpdir="hpcviewer.tmp"
	rm -rf $tmpdir; mkdir $tmpdir; cd $tmpdir
	tar xzf ../$output

	cd hpcviewer

	dirinstall="/home/$USER/pkgs/hpctoolkit"
	./install $dirinstall

	echo "has bin installed at $dirinstall successfully"
	#$dirinstall/bin/hpcviewer
else
	echo "File doesn't exist: $output"
	cat log.err
fi
