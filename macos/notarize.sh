#!/bin/zsh

check_file() {
if [ ! -f $1 ]; then
	die "hpcviewer mac file doesn't exist: $1"
fi
}

FILE=`ls hpcviewer-*-macosx.cocoa.x86_64.zip`
check_file $FILE

DIR_CONFIG="macos"
BASE_CONFIG="config.hcl"
FILE_CONFIG="${DIR_CONFIG}/${BASE_CONFIG}"
FILE_PLIST="${DIR_CONFIG}/p.plist"

check_file $FILE_CONFIG
check_file $FILE_PLIST

# check the AC_PASSWORD env
if [[ -z "${AC_PASSWORD}" ]] ; then
	die "AC_PASSWORD env is not set"
fi

DIR_PREP="prepare_sign"

rm -rf $DIR_PREP
mkdir $DIR_PREP

cp $FILE_CONFIG $DIR_PREP
cp $FILE_PLIST  $DIR_PREP

cd $DIR_PREP
unzip ../$FILE

#gon $BASE_CONFIG
