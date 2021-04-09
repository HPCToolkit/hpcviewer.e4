#!/bin/zsh

if ! command -v gon &> /dev/null
then 
	echo "gon is not installed. Unable to continue the script"
	echo "Please install gon from https://github.com/mitchellh/gon" 
	exit
fi

check_file() {
if [ ! -f $1 ]; then
	echo "hpcviewer mac file doesn't exist: $1"
	exit 1
fi
}

FILE=$1
if [[ "$1" == ""  ]]; then
	FILE=`ls hpcviewer-*-macosx.cocoa.x86_64.zip | tail -n 1`
fi
echo "File to be notarized: $FILE"
check_file $FILE

FILE_BASE="${FILE%.zip}"
echo "File base: ${FILE_BASE}"

DIR_CONFIG="macos"
BASE_CONFIG="config.hcl"
FILE_PLIST="${DIR_CONFIG}/p.plist"

check_file $FILE_PLIST

# check the AC_PASSWORD env
if [[ -z "${AC_PASSWORD}" ]] ; then
	echo "AC_PASSWORD env is not set"
	exit 1
fi

DIR_PREP="prepare_sign"

rm -rf $DIR_PREP
mkdir $DIR_PREP

cat << EOF > $DIR_PREP/$BASE_CONFIG
source = ["./hpcviewer.app"]
bundle_id = "edu.rice.cs.hpcviewer"

apple_id {
  username = "la5@rice.edu"
  password = "@env:AC_PASSWORD"
}

sign {
  application_identity = "Developer ID Application: Laksono Adhianto"
  entitlements_file = "p.plist"
}

dmg {
  output_path = "${FILE_BASE}.dmg"
  volume_name = "hpcviewer"
}

EOF

cp $FILE_PLIST  $DIR_PREP

cd $DIR_PREP
unzip -q ../$FILE

#gon $BASE_CONFIG

cd ..
echo "Notarized file: $DIR_PREP/${FILE_BASE}.dmg"
