#!/bin/bash

# Symbolicate a crash using Xcode 5

ARGS=("$@")

XCODE_DIR="/Applications/Xcode.app"
export DEVELOPER_DIR="${XCODE_DIR}/Contents/Developer"

CRASH="${DEVELOPER_DIR}/Platforms/iPhoneOS.platform/Developer/Library/PrivateFrameworks/DTDeviceKitBase.framework/Versions/Current/Resources/symbolicatecrash"

$CRASH -v $ARGS
