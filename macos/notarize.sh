#!/bin/zsh

if ! command -v gon &> /dev/null
then 
	echo "Error: gon is not installed. Unable to continue the script"
	echo "Please install gon from https://github.com/mitchellh/gon" 
	exit
fi

#
# check if a file exists. If not, just quit
#
check_file() {
if [ ! -f $1 ]; then
	echo "Error: hpcviewer mac file doesn't exist: $1"
	exit 1
fi
}

FILE=$1
if [[ "$1" == ""  ]]; then
#	FILE=`ls hpcviewer-*-macosx.cocoa.x86_64.zip | tail -n 1`
 	echo "Syntax: $0 <file_to_notarize>"
	exit 1
fi
echo "Notarize: $FILE"
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

##############################
# 
# Prepare the configuration script
#
##############################

DIR_PREP="prepare_sign-$FILE"

if [ -e $DIR_PREP ]; then
	echo "Warning: Directory $DIR_PREP already exist, and it will be replaced."
 	echo "Enter any key to continue ..."
	read
fi
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

zip {
  output_path = "${FILE_BASE}.zip"
}

EOF

cp $FILE_PLIST  $DIR_PREP

cd $DIR_PREP
unzip -q ../$FILE

##############################
#
# Notarize the viewer
#
##############################
gon $BASE_CONFIG

##############################
# 
# Hack: create the zip file for notarized file
# Somehow, gon doesn't create hpcviewer.app directory 
# inside the zip file
#
##############################

mkdir -p tmp/hpcviewer.app
cd tmp/hpcviewer.app
unzip ../../"${FILE_BASE}.zip"
cd ..
zip -r "${FILE_BASE}.zip" hpcviewer.app
cd ..
mv "${FILE_BASE}.zip" ..

##############################
#
# END
# 
##############################

cd ..
ls -l "$DIR_PREP/${FILE_BASE}.*"
