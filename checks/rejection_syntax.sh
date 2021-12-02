#!/bin/bash

readonly TIMEOUT=3 # seconds

readonly THIS_DIR="$(dirname "$0")"
readonly MINIC="$THIS_DIR/../minic.sh"

function usage {
  echo "USAGE: $0 [MINIC_OPTIONS...] PROGRAM_PATH" >&2
}

if [ $# -lt 1 ] ; then
  usage
  exit 0 # sic
fi

# extract path to input program (last argument)
readonly INPUT_PROGRAM="${@: -1}"
set -- "${@:1:$(($#-1))}"

if [ ! -f "$INPUT_PROGRAM" ] ; then
  usage
  exit 0 # sic
fi

# implementation under test
timeout "$TIMEOUT" "$MINIC" "$INPUT_PROGRAM" "$@" 2> /dev/null > /dev/null
exit_code="$?"

if [[ "$exit_code" -eq 130 || "$exit_code" -eq 131 ]] ; then
  echo "[i] rejected" >&2
  exit 1
fi
