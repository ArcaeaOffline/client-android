#!/usr/bin/env bash
# Verifies the built APKs use the custom-built onnxruntime/opencv
# from maven-local/ instead of silently falling back to the
# official artifacts.
#
# Soft-fail: mismatches are reported as warnings in the job summary,
# but don't terminate the build.
set -euo pipefail

# .github/scripts/ -> repo root
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
MAVEN_LOCAL="${MAVEN_LOCAL:-$ROOT/maven-local}"
APK_DIR="${APK_DIR:-$ROOT/app/build/outputs/apk/unstable/release}"

# <so name>|<AAR path relative to maven-local>
# Keep in sync with .github/actions/setup-custom-maven/action.yml
LIBS=(
    "libonnxruntime.so|com/microsoft/onnxruntime/onnxruntime-android/1.26.0/onnxruntime-android-1.26.0.aar"
    "libopencv_java5.so|org/opencv/opencv/5.0.0/opencv-5.0.0.aar"
)
# universal APK is the union of these four, so no special checks for it
ABIS=(arm64-v8a armeabi-v7a x86 x86_64)

# sha256 of a file inside a zip; empty output if the entry is missing
# $1=zip file, $2=path inside zip
file_in_zip_sha256() {
    if ! unzip -l "$1" "$2" >/dev/null 2>&1; then
        return 1
    fi
    unzip -p "$1" "$2" | sha256sum | cut -d' ' -f1
}

warnings=()
for abi in "${ABIS[@]}"; do
    apk="$APK_DIR/app-unstable-$abi-release.apk"
    if [ ! -f "$apk" ]; then
        warnings+=("missing APK: $apk")
        continue
    fi
    for entry in "${LIBS[@]}"; do
        so="${entry%%|*}"
        aar="$MAVEN_LOCAL/${entry#*|}"
        custom="$(file_in_zip_sha256 "$aar" "jni/$abi/$so" || true)"
        packed="$(file_in_zip_sha256 "$apk" "lib/$abi/$so" || true)"
        if [ -z "$custom" ]; then
            warnings+=("$abi/$so: custom .so not found in $aar")
        elif [ "$custom" != "$packed" ]; then
            warnings+=("$abi/$so: maven-local ${custom:0:12}.. != APK ${packed:0:12}..")
        fi
    done
done

if [ "${#warnings[@]}" -gt 0 ]; then
    for w in "${warnings[@]}"; do
        echo "::warning::$w"
    done
    {
        echo "## Custom build verification failed"
        echo ""
        echo "The APKs do not match the custom-built libraries in maven-local/."
        echo ""
        echo "| Check |"
        echo "| --- |"
        for w in "${warnings[@]}"; do
            echo "| $w |"
        done
    } >>"${GITHUB_STEP_SUMMARY:-/dev/null}"
    # Non-zero exit marks the step yellow; the workflow step uses
    # continue-on-error so this never blocks artifact upload/release.
    exit 1
fi
exit 0
