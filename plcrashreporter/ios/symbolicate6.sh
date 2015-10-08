#!/bin/bash

# Symbolicate a crash using Xcode 6

ARGS=("$@")
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
XCODE_DIR="/Applications/Xcode.app"
export DEVELOPER_DIR="${XCODE_DIR}/Contents/Developer"

CRASH="symbolicatecrash"


$CRASH -v $ARGS
