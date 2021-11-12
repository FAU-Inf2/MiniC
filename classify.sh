#!/bin/bash

java -Xss2m -ea -cp "$(dirname $0)/build/libs/MiniC.jar" \
  i2.act.examples.minic.Classifier "$@"
