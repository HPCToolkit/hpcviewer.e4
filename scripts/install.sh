#!/bin/sh
#
# Copyright (c) 2002-2022, Rice University.
# See the file README.License for details.
#
# Install hpcviewer into hpctoolkit directory.
# This script is only for Unix and X windows.
#
# Usage: ./install [-f] [-j java-dir] install-dir
#
# where 'java-dir' is a Java jdk or jre directory,
# and 'install-dir' is the directory in which to install.
# Use -f to skip Java sanity checks.
#
# Run this script from its own directory as ./install.
#
# $Id$
#

name=hpcviewer
install_subdirs='configuration plugins p2 features'

#------------------------------------------------------------
# Error messages
#------------------------------------------------------------

die()
{
    echo "$0: $*" 1>&2
    exit 1
}

usage()
{
    cat <<EOF
Usage: ./install [-f] [-j java-dir] install-dir

where 'java-dir' is a Java jdk or jre directory,
and 'install-dir' is the directory in which to install.
Use -f to skip Java sanity checks.

Run this script from its own directory as ./install.
EOF
    exit 0
}

#------------------------------------------------------------
# Command-Line Options
#------------------------------------------------------------

java_dir=
install_dir=
force=no

while test -n "$1"
do
    arg="$1" ; shift
    case "$arg" in
	-h | -help | --help )
	    usage
	    ;;
	-f | -force | --force )
	    force=yes
	    ;;
	-j | -java | --java )
	    test "x$1" != x || die "missing directory for $arg"
	    java_dir="$1"
	    shift
	    ;;
	-* )
	    die "unknown option: $arg"
	    ;;
	* )
	    install_dir="$arg"
	    break
	    ;;
    esac
done

test "x$install_dir" != x || usage

#------------------------------------------------------------
# Java tests
#------------------------------------------------------------

# Java_dir is the jdk or jre directory, if specified.

java_bindir=
if test -n "$java_dir" ; then
    if test -f "${java_dir}/bin/java" ; then
	  java_bindir="${java_dir}/bin"
    elif test -f "${java_dir}/java" ; then
	  java_bindir="${java_dir}"
    else
	  die "unable to find java in: $java_dir"
    fi
    PATH="${java_bindir}:$PATH"
fi

# Find java VM size that will run.  On some systems, the shell limits
# are too small and we need to reset the java VM size.

java_vmsize=
java_version=`java -version 2>&1 \
	  | head -1 \
	  | cut -d'"' -f2 \
	  | sed 's/^1\.//' \
	  | cut -d'.' -f1`
for size in 2048 1536 1024 512 256
do
    if test -n "$java_version" ; then
	java_vmsize="$size"
	break
    fi
done

# Check that version is 17 or later.

echo "Java version $java_version"
jvm_required=17

if test "$force" = no ; then
    if test "x$java_version" = x ; then
	  echo "Warning: unable to find program 'java' on your PATH"
	  echo "Please install java before launching hpcviewer"
	  echo "Enter to continue ..."
	  read keysomething
    elif test $java_version -lt "${jvm_required}" ; then
	  echo "$java_version is too old, use Java ${jvm_required} or later"
	  echo "Enter to continue ..."
	  read keysomething
    fi
fi

#------------------------------------------------------------
# Install the viewer and reset permissions.
#------------------------------------------------------------

bin_dir="${install_dir}/bin"
libexec_dir="${install_dir}/libexec/$name"
binary_file="$name"
ini_file="${name}.ini"
sh_file="${name}_launcher.sh"

# bug no 20: https://github.com/HPCToolkit/hpcviewer/issues/20
if [ -e $libexec_dir ]; then
  rm -rf $libexec_dir
fi

mkdir -p "$bin_dir" "$libexec_dir" \
    || die "unable to mkdir install directories"

echo "copying files ..."

tar cf - $install_subdirs | ( cd "$libexec_dir" && tar xvf - )
test $? -eq 0 || die "unable to install libexec files"

cp -f "$sh_file" "${bin_dir}/$name" \
    || die "unable to install bin files"

cp -f "$binary_file" "$ini_file" "$libexec_dir" \
    || die "unable to install libexec files"

# Reset JAVA_BINDIR in hpcviewer launch script.
if test -n "$java_bindir" ; then
    echo "setting JAVA_BINDIR=${java_bindir}"
    orig="${bin_dir}/${name}"
    tmp="${orig}.tmp"
    rm -f "$tmp"
    mv -f "$orig" "$tmp"
    sed -e "s,^#.*JAVA_BINDIR=.*\$,JAVA_BINDIR='${java_bindir}'," \
	<"$tmp"  >"$orig"
    rm -f "$tmp"
fi

# Reset -Xmx java VM size in hpcviewer.ini file.
if test -n "$java_vmsize" ; then
    echo "setting java VM size to ${java_vmsize} Meg"
    orig="${libexec_dir}/${ini_file}"
    tmp="${orig}.tmp"
    rm -f "$tmp"
    mv -f "$orig" "$tmp"
    sed -e "s,^.*-Xmx.*\$,-Xmx${java_vmsize}m," \
	<"$tmp"  >"$orig"
    rm -f "$tmp"
fi

# Note: the find idiom below is used to handle file names with
# embedded spaces.

echo "resetting permissions ..."
chmod a+rx "${bin_dir}/$name" "${libexec_dir}/$binary_file"
find "$libexec_dir" -type d -exec chmod a+rx {} \;
find "$libexec_dir" -type f -exec chmod a+r  {} \;

echo "done"
exit 0
