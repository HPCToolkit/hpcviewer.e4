#!/bin/sh

trap 'kill -s TERM $PID_WM' TERM INT QUIT

case "$WM" in
metacity)
  USE_XVFB=1
  WM_CMD="metacity"
  ;;
*)
  echo "Unrecognized WM value '$WM'!" >&2
  exit 1
  ;;
esac

if test -n "$USE_XVFB"; then
  Xvfb -ac -listen tcp :99 & PID_XVFB=$!
  export DISPLAY=:99
fi

$WM_CMD & PID_WM=$!

wait $PID_WM
STATUS_WM=$?

if test -n "$USE_XVFB"; then
  # We kill the X server whenever the WM dies, for any reason
  kill -s TERM "$PID_XVFB"
  wait "$PID_XVFB"
  STATUS_XVFB=$?

  if test $STATUS_XVFB -ne 0; then
    exit $STATUS_XVFB
  fi
fi

exit $STATUS_WM
