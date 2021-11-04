#!/bin/bash

set -e

readonly THIS_DIR="$(dirname "$0")"

function usage {
  echo "USAGE: $0 <path to *Smith jar> <class name> <program options>" >&2
}

if [[ $# -lt 2 || "$1" == "--help" ]] ; then
  usage
  exit 1
fi

STARSMITH_JAR="$1"
shift

if [ ! -f "$STARSMITH_JAR" ] ; then
  echo "[!] could not find *Smith jar file in $STARSMITH_JAR"
  exit 1
fi

STARSMITH_JAR="$(realpath "$STARSMITH_JAR")"

java -ea -cp "$STARSMITH_JAR":"$THIS_DIR/classes":"$THIS_DIR" "$@"
