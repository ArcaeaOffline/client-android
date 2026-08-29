#!/usr/bin/env bash
# setup-custom-maven.sh
#
# Downloads the custom-built onnxruntime/opencv maven artifacts from the
# custom-lib-builds stable release tags and extracts them into maven-local/.
# maven-local/ uses the same GAVs as the official artifacts, so Gradle
# resolves the custom builds first (settings.gradle.kts local repo block).
#
# Hard-fail: a download error aborts the build. Falling back to the official
# artifacts would ship an APK that differs from what was verified locally.
set -euo pipefail

# .github/scripts/ -> repo root
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MAVEN_LOCAL="${MAVEN_LOCAL:-$ROOT/maven-local}"

# Stable tags, overwritten on publish (URL stays the same, content changes).
# No actions/cache: a version-based cache key would serve stale artifacts
# after a republish. GitHub Actions has fast access to its own releases,
# so we just re-download (~125MB) on every run.
ORT_TAG="${ORT_TAG:-onnxruntime-1.26.0}"
OPENCV_TAG="${OPENCV_TAG:-opencv-5.0.0}"
BASE_URL="https://github.com/ArcaeaOffline/custom-lib-builds/releases/download"

mkdir -p "$MAVEN_LOCAL"

download_and_extract() { # $1=tag, $2=local zip path
    curl -fL -o "$2" "$BASE_URL/$1/maven_repo.zip"
    unzip -q -o "$2" -d "$MAVEN_LOCAL"
    rm -f "$2"
}

download_and_extract "$ORT_TAG" /tmp/ort-maven.zip
download_and_extract "$OPENCV_TAG" /tmp/opencv-maven.zip

# The two zips have com/ and org/ roots; they merge into maven-local/
# without conflicts. Verify the expected AARs actually landed.
test -f "$MAVEN_LOCAL/com/microsoft/onnxruntime/onnxruntime-android/1.26.0/onnxruntime-android-1.26.0.aar"
test -f "$MAVEN_LOCAL/org/opencv/opencv/5.0.0/opencv-5.0.0.aar"
