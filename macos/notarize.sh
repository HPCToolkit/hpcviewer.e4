#!/bin/zsh


##############################
#
# check if files and variables exist. If not, just quit
#
# Param:
#   the name of hpcviewer zip file to be notarized
#
# Environment variables:
#   AC_USERID : Apple user id
#   AC_PASSWORD: Apple specific application password
##############################

die() {
	echo "$1"
	exit 1
}

check_file() {
if [ ! -f $1 ]; then
	die "Error: hpcviewer mac file doesn't exist: $1"
fi
}

FILE=$1
if [[ "$1" == ""  ]]; then
#	FILE=`ls hpcviewer-*-macosx.cocoa.x86_64.zip | tail -n 1`
 	die "Syntax: $0 <file_to_notarize>"
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
	die "AC_USERID env is not set. It's required to sign the application"
fi

# check the AC_PASSWORD env
if [[ -z "${AC_PASSWORD}" ]] ; then
	die "AC_PASSWORD env is not set"
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
# Notarize and staple the viewer
#
##############################
notarize_app() {
	FILENAME="$1"
	NOTARIZE="$2"

	echo ""
	echo "Notarization started: ${FILENAME}"	
	xcrun notarytool submit "${FILENAME}" --keychain-profile "${NOTARIZE}" --wait

	echo "Stapling the notarization ticket"
	staple="$(xcrun stapler staple "${FILENAME}")"
	if [ $? -eq 0 ]; then
		echo "The disk image is now notarized"
	else
		echo "$staple"
		echo "The notarization failed with error $?"
	fi
}


######
# code sign the application
# params:
#   the folder of the application
######
sign_app() {
	SOURCE_FOLDER="$1"
	echo "codesign ${SOURCE_FOLDER}"
	codesign -f  -s "${AC_USERID}"  --options=runtime   --entitlements  "p.plist"  "${SOURCE_FOLDER}"
}


######
# function to create a dmg file
# params:
#   full path of the dmg filename to be created
#   full path of the folder of the source to be packed into an image
######
create_dmg() {
	FILENAME="$1"
	SOURCE_FOLDER="$2"
	echo "Create ${FILENAME} from ${SOURCE_FOLDER}"

	../${DIR_CONFIG}/create-dmg \
		   --no-internet-enable \
		   --volname "hpcviewer" \
		   --volicon hpcviewer.app/Contents/Resources/hpcviewer.icns \
		   "${FILENAME}" \
		   "${SOURCE_FOLDER}"

	check_file "${FILENAME}"
}


######
# function to create a zip file
# params:
#   full path of the zip filename to be created
#   full path of the folder of the source to be zipped
######
create_zip() {
	FILENAME="$1"
	SOURCE_FOLDER="$2"
	echo "Create ${FILENAME} from ${SOURCE_FOLDER}"
	
	ditto -c -k --sequesterRsrc --keepParent ${SOURCE_FOLDER} ${FILENAME}
}

[[ -d "hpcviewer.app" ]] || die "Not found: hpcviewer.app"

######
# code signature of hpcviewer for both *.dmg and *.zip
#####
sign_app "hpcviewer.app"

######
# create *.dmg file and notarize
######
create_dmg  "${FILE_BASE}.dmg"  "hpcviewer.app/"
notarize_app "${FILE_BASE}.dmg"  AC_PASSWORD

######
# create *.zip file and notarize
######
create_zip  "${FILE_BASE}.zip"  "hpcviewer.app/"
notarize_app "${FILE_BASE}.zip"  AC_PASSWORD


##############################
#
# END
# 
##############################

echo cp hpcviewer-*.dmg  hpcviewer-*.zip ..
cp hpcviewer-*.dmg  hpcviewer-*.zip ..

cd ..
ls -l "$DIR_PREP/${FILE_BASE}.*"

