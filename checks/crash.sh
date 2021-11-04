#!/bin/bash

readonly TIMEOUT=10 # seconds

readonly THIS_DIR="$(dirname "$0")"
readonly MINIC="$THIS_DIR/../run.sh"

if [[ $# -lt 1 || ! -f "$1" ]] ; then
  echo "USAGE: $0 PROGRAM_PATH [MINIC_OPTIONS...]" >&2
  exit 0 # sic
fi

readonly INPUT_PROGRAM="$1"
shift

timeout "$TIMEOUT" "$MINIC" --in "$INPUT_PROGRAM" --interpret $@ 2> /dev/null > /dev/null
exit_code="$?"

if [ "$exit_code" -eq 124 ] ; then
  # timeout
  exit 0
fi

if [ "$exit_code" -eq 1 ] ; then
  exit 1 # crash
else
  exit 0 # no crash
fi
