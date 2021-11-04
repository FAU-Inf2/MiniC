#!/bin/bash

readonly TIMEOUT=10 # seconds

readonly THIS_DIR="$(dirname "$0")"
readonly MINIC="$THIS_DIR/../run.sh"

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
timeout "$TIMEOUT" "$MINIC" --in "$INPUT_PROGRAM" "$@" 2> /dev/null > /dev/null
exit_code="$?"

case "$exit_code" in
"124")
  echo "[i] timeout" >&2
  exit 0
  ;;
"1")
  echo "[!] crash" >&2
  exit 1
  ;;
esac
