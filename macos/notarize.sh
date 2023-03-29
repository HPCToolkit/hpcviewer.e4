#!/bin/zsh


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
FILE_PLIST="${DIR_CONFIG}/p.plist"

check_file $FILE_PLIST

# check the AC_USER env
if [[ -z "${AC_USERID}" ]] ; then
	echo "AC_USERID env is not set. It's required to sign the application"
	exit 1
fi

# check the AC_PASSWORD env
if [[ -z "${AC_PASSWORD}" ]] ; then
	echo "AC_PASSWORD env is not set"
	exit 1
fi

##############################
# 
# Prepare the folder and unzip the app
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

cp $FILE_PLIST  $DIR_PREP

if [[ ! -e "share" ]]; then
	mkdir -p share/create-dmg
	echo ln -s "${DIR_CONFIG}/support" share/create-dmg/
	cp -R "${DIR_CONFIG}/support" share/create-dmg/
fi

cd $DIR_PREP
unzip -q ../$FILE

##############################
#
# Notarize the viewer
#
##############################
notarize() {
	FILENAME="$1"
	echo ""

	echo "codesign hpcviewer.app"
	codesign -f  -s "${AC_USERID}"  --options=runtime   --entitlements  "p.plist"  "hpcviewer.app"

	echo "Create and notarize ${FILENAME}"
	../${DIR_CONFIG}/create-dmg \
		   --no-internet-enable \
		   --volname "hpcviewer" \
		   --volicon hpcviewer.app/Contents/Resources/hpcviewer.icns \
		   --notarize AC_PASSWORD \
		   "${FILENAME}" \
		   "hpcviewer.app/"

	check_file "${FILENAME}"
}

notarize "${FILE_BASE}.dmg"



##############################
#
# END
# 
##############################

cd ..
ls -l "$DIR_PREP/${FILE_BASE}.*"

