#!/bin/bash

readonly TIMEOUT=10 # seconds
readonly MAX_SIZE=1024 # byte

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
reference_output=$(\
  trap "" PIPE ; \
  timeout "$TIMEOUT" "$MINIC" --in "$INPUT_PROGRAM" --interpret 2> /dev/null | \
    head -c $((MAX_SIZE + 1)) ; \
  exit ${PIPESTATUS[0]})

reference_exit_code=$?
reference_size="${#reference_output}"

if [ "$reference_exit_code" -ne 0 ] || [ "$reference_size" -gt "$MAX_SIZE" ]  ; then
  exit 0 # timeout or invalid program
fi

# implementation under test
test_output=$(\
  trap "" PIPE ; \
  timeout "$TIMEOUT" "$MINIC" --in "$INPUT_PROGRAM" --interpret $@ 2> /dev/null | \
    head -c $((MAX_SIZE + 1)) ; \
  exit ${PIPESTATUS[0]})

test_exit_code=$?

# check for difference in exit code
if [ "$test_exit_code" -ne "$reference_exit_code" ] ; then
  echo "[i] wrong exit code ($test_exit_code vs. $reference_exit_code)" >&2
  exit 1
fi

# check for difference in output
if ! diff -q <( echo "$reference_output" ) <( echo "$test_output" ) 2>&1 > /dev/null ; then
  echo "[i] wrong output" >&2
  exit 1
fi

# outputs are the same; however, we consider 'UNDEF' output as a difference
if grep -q "UNDEF" <(echo "$reference_output" ) ; then
  echo "[i] undefined output" >&2
  exit 1
fi
