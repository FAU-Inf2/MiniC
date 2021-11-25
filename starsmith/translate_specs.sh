#!/bin/bash

set -e

readonly THIS_DIR="$(dirname "$0")"

readonly COLOR_YELLOW="\033[1;33m"
readonly COLOR_NONE="\033[0m"

function print_step {
  printf "${COLOR_YELLOW}======[ ${1} ]======${COLOR_NONE}\n"
}

function usage {
  echo "[!] USAGE: $0 <path to *Smith jar>" >&2
}

if [[ $# -ne 1 || "$1" == "--help" ]] ; then
  usage
  exit 1
fi

STARSMITH_JAR="$1"

if [ ! -f "$STARSMITH_JAR" ] ; then
  echo "[!] could not find *Smith jar file in $STARSMITH_JAR"
  exit 1
fi

STARSMITH_JAR="$(realpath "$STARSMITH_JAR")"

# TRANSLATE THE RUNTIME CLASSES
print_step "translate runtime classes"

pushd "$THIS_DIR/out/runtime" > /dev/null
javac -cp "$STARSMITH_JAR":./ *.java
popd > /dev/null

# TRANSLATE THE LALA SPECIFICATIONS
print_step "translate specifications"

function translate_spec() { # <spec file> <java file> <max depth>
  spec_file="$THIS_DIR/specs/$1"
  java_file="$THIS_DIR/out/$2"
  max_depth="$3"

  echo "- $spec_file => $java_file"

  java -ea -jar "$STARSMITH_JAR" \
    --spec "$spec_file" \
    --maxDepth "$max_depth" \
    --allFeatures \
    --toJava "$java_file"

  pushd "$THIS_DIR/out" > /dev/null

  class_dir="classes/"
  mkdir -p "$class_dir"
  javac -cp "$STARSMITH_JAR":"./" -d "$class_dir" "$(basename "$java_file")"

  popd > /dev/null
}

translate_spec "minic.ls"       "minic.java"       25
translate_spec "minic_undef.ls" "minic_undef.java" 25
