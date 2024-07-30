#!/bin/bash
#
# Copyright (c) 2002-2022, Rice University.
# See the file README.License for details.
#
# Launch the viewer binary and set the workspace directory.
# This script is only for Unix and X windows.
#
# $Id$
#

name=hpcviewer
arch=`uname -m`
workspace="${HOME}/.hpctoolkit/${name}/${arch}"

# Substitute the Java bindir from the install script, if needed.
# JAVA_BINDIR=/path/to/java/bindir

if test -d "$JAVA_BINDIR" ; then
    PATH="${JAVA_BINDIR}:$PATH"
fi

#------------------------------------------------------------
# Error messages
#------------------------------------------------------------

die()
{
    echo "$0: error: $*" 1>&2
    exit 1
}

warn()
{
    echo "$0: warning: $*" 1>&2
}

#------------------------------------------------------------
# help display
#------------------------------------------------------------


usage()
{
    cat <<EOF
Usage:
  hpcviewer [viewer-options] [database-directory]

Options:
  -h, --help               Print help.

  -jh, --java-heap <size>  Set the JVM maximum heap size. The value of <size> must be
                           in m (megabytes) or g (gigabytes). Example:
                              hpcviewer  -jh 3g
                           will use a JVM maximum heap size of 3GB for this execution
                           of hpcviewer.
                           
  -v, --version            Print the current version 
EOF
}


#------------------------------------------------------------
# print the application version
#------------------------------------------------------------

print_version()
{
   # Note:
   #   the install script should replace the variable version automatically
   #
   echo "__VERSION__"
   exit 1
}

#------------------------------------------------------------
# Find the hpctoolkit directory.
#------------------------------------------------------------

script="$0"
if test -L "$script" ; then
    script=`readlink "$script"`
fi
bindir=`dirname "$script"`
bindir=`( cd "$bindir" && pwd )`
viewer="${bindir}/../libexec/${name}/${name}"
test -x "$viewer"  || die "executable $viewer not found"

#------------------------------------------------------------
# Check the java version.
#------------------------------------------------------------
JAVA=`type -p java`

if [ "$JAVA"x != "x" ]; then
    JAVA=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo found java executable in JAVA_HOME     
    JAVA="$JAVA_HOME/bin/java"
else
    die "unable to find program 'java' on your PATH"
fi

JAVA_MAJOR_VERSION=`java -version 2>&1 \
	  | head -1 \
	  | cut -d'"' -f2 \
	  | sed 's/^1\.//' \
	  | cut -d'.' -f1`

echo "Java version $JAVA_MAJOR_VERSION"

# we need Java 17 at least
jvm_required=17

if [ "$JAVA_MAJOR_VERSION" -lt "$jvm_required" ]; then
	die "$name requires Java $jvm_required"
fi



#------------------------------------------------------------
# Check GTK version
#------------------------------------------------------------

if ! command -v gtk-launch &> /dev/null
then
    echo "It seems GTK-3 is not installed."
    echo "$name requires GTK 3.20 or newer."
    echo "Do you want to continue ? [Y/n]"
    read conti
    if [ "$conti" != "Y"  ]; then
       exit
    fi
fi


#------------------------------------------------------------
# Look for arguments
#------------------------------------------------------------

viewer_args=""
PARAMS=""
while (( "$#" )); do
  case "$1" in 
    -h|--help)
      usage
      exit 0
      ;;
      
    -jh|--java-heap)
      if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
        viewer_args="-vmargs -Xmx$2"
        shift 2
      else
        echo "Error: Argument for $1 is missing" >&2
        exit 1
      fi
      ;;
      
    -v|--version)
      print_version
      ;;

    *) # preserve positional arguments
      PARAMS="$PARAMS $1"
      shift
      ;;
  esac
done
# set positional arguments in their proper place
eval set -- "$PARAMS"

#
# make sure we can display X on linux
#
test -n "$DISPLAY" || die "DISPLAY variable is not set"

#------------------------------------------------------------
# Prepare the environment.
#------------------------------------------------------------

# UBUNTU's unity menu is broken and only displays hpcviewer's
# file menu. Address this by disabling UBUNTU's unity menus.
# This setting is harmless on non-UBUNTU platforms.
# export UBUNTU_MENUPROXY=0

# Eclipse Mars has many issues with GTK3, used by default
# to go back to GTK2, we need to set variable environment before
# running the viewer. This variable seems harmless for other 
# applications (except SWT-based programs)
# see bugs on 
#  https://bugs.eclipse.org/bugs/show_bug.cgi?id=470994
#  https://bugs.eclipse.org/bugs/show_bug.cgi?id=478962
#  https://bugs.eclipse.org/bugs/show_bug.cgi?id=470994
# export SWT_GTK3=0

# Fix issue with GTK3 overlay scrolling by forcing the window
# manager to display the scroll bars instead of hiding them
# automatically
export GTK_OVERLAY_SCROLLING=0 

#------------------------------------------------------------
# Launch the viewer.
#------------------------------------------------------------

if test -d "$HOME" ; then
    stderr="$workspace"/hpcviewer.err
    mkdir -p "$workspace"
    echo "Redirect standard error to $stderr"
    cmd="$viewer -data $workspace -configuration $workspace $PARAMS  $viewer_args  " 
    exec $cmd 2> $stderr
else
    warn "HOME is not set, proceeding anyway"
    exec "$viewer" "$@"
fi
