#!/usr/bin/env bash
# verify-apk-signing.sh
#
# Verifies signatures of all unstableRelease APKs.
# Hard-fail: unsigned or invalidly signed APKs must abort the build.
set -euo pipefail

# Use the newest build-tools for this run
BUILD_TOOLS_VERSION="$(find "$ANDROID_HOME/build-tools" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | sort -V | tail -n 1)"
PATH="$ANDROID_HOME/build-tools/$BUILD_TOOLS_VERSION:$PATH"

APK_DIR="${APK_DIR:-app/build/outputs/apk/unstable/release}"

for apk in \
    "$APK_DIR/app-unstable-arm64-v8a-release.apk" \
    "$APK_DIR/app-unstable-armeabi-v7a-release.apk" \
    "$APK_DIR/app-unstable-x86_64-release.apk" \
    "$APK_DIR/app-unstable-x86-release.apk" \
    "$APK_DIR/app-unstable-universal-release.apk"; do
    apksigner verify --verbose --min-sdk-version 24 "$apk"
done
