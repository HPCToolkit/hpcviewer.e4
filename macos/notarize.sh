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

show_help(){
	echo "Syntax: $0 [-options] <hpcviewer_zip_file>"
	echo "Options: "
	echo "   -i  --image    Just create image file, no notarization"
	exit 1
}


die() {
	echo "$1"
	exit 1
}


check_file() {
	if [ ! -f $1 ]; then
		die "Error: hpcviewer mac file doesn't exist: $1"
	fi
}

NOTARIZATION=1
FILE=$1
if [[ "$1" == ""  ]]; then
 	show_help
elif [[ "$1" == "-i" || "$1" == "--image" ]]; then
	NOTARIZATION=0
	FILE="$2"
fi

echo "Notarize: $FILE"
check_file $FILE

FILE_BASE="${FILE%.zip}"
echo "File base: ${FILE_BASE}"

DIR_CONFIG="macos"
FILE_PLIST="${DIR_CONFIG}/p.plist"

check_file $FILE_PLIST

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
cp "${DIR_CONFIG}/arrow.png" $DIR_PREP


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
#   mode: 1 for batch mode (no jenkins). Default: 0
######
create_dmg() {
	FILENAME="$1"
	SOURCE_FOLDER="$2"
	MODE="--hdiutil-quiet"

	if [ "$3" != "" ]; then
		MODE="--skip-jenkins"
	fi

	echo "Create ${FILENAME} from ${SOURCE_FOLDER} with mode:$MODE"

	../${DIR_CONFIG}/create-dmg "$MODE"  \
		   --no-internet-enable \
		   --background "arrow.png" \
		   --volname "hpcviewer" \
		   --window-pos 200 120 \
		   --window-size 600 300 \
		   --icon-size 100 \
		   --icon "hpcviewer.app" 175 120 \
		   --app-drop-link 425 120 \
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
# Case for -i or --image
#  no need to sign and notarize. Just create an image file
######
if [[ "$NOTARIZATION" == "0" ]]; then
	create_dmg  "${FILE_BASE}.dmg"  "hpcviewer.app/"  1
	cp "${FILE_BASE}.dmg"  ..
	cd ..
	ls -l *.dmg
	exit 0
fi


######
# code signature of hpcviewer for both *.dmg and *.zip
#####
# check the AC_USER env
if [[ -z "${AC_USERID}" ]] ; then
	die "AC_USERID env is not set. It's required to sign the application"
fi

# check the AC_PASSWORD env
if [[ -z "${AC_PASSWORD}" ]] ; then
	die "AC_PASSWORD env is not set"
fi

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

