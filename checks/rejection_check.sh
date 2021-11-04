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

# reference implementation
timeout "$TIMEOUT" "$MINIC" --in "$INPUT_PROGRAM" 2> /dev/null > /dev/null
exit_code="$?"

if [ "$exit_code" -ne 0 ] ; then
  case "$exit_code" in
  "124")
    echo "[i] timeout in reference implementation" >&2
    ;;
  *)
    echo "[i] program is invalid" >&2
    ;;
  esac

  exit 0
fi

# implementation under test
timeout "$TIMEOUT" "$MINIC" --in "$INPUT_PROGRAM" $@ 2> /dev/null > /dev/null
exit_code="$?"

if [ "$exit_code" -ne 0 ] ; then
  echo "[i] rejected" >&2
  exit 1
fi
