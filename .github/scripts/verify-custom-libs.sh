#!/usr/bin/env bash
# Verifies the built APKs use the custom-built onnxruntime/opencv
# from maven-local/ instead of silently falling back to the
# official artifacts.
#
# Size is the primary signal: the custom .so files are ~50% the size
# of the official ones, far outside the +/-10% tolerance.
# sha256 hashes are reported for reference only: release build may
# strip on the .so files, so hashes legitimately differ there.
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

# size of a file inside a zip; empty output if the entry is missing
# $1=zip file, $2=path inside zip
file_in_zip_size() {
    if ! unzip -l "$1" "$2" >/dev/null 2>&1; then
        return 1
    fi
    unzip -p "$1" "$2" | wc -c
}

warnings=()
rows=()
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
        custom_size="$(file_in_zip_size "$aar" "jni/$abi/$so" || true)"
        packed_size="$(file_in_zip_size "$apk" "lib/$abi/$so" || true)"

        result="OK"
        if [ -z "$custom" ] || [ -z "$custom_size" ]; then
            warnings+=("$abi/$so: custom .so not found in $aar")
            result="NOT FOUND"
        elif [ -z "$packed" ] || [ -z "$packed_size" ]; then
            warnings+=("$abi/$so: .so not found in APK")
            result="NOT FOUND"
        elif [ "$packed_size" -lt $((custom_size * 90 / 100)) ] || \
             [ "$packed_size" -gt $((custom_size * 110 / 100)) ]; then
            warnings+=("$abi/$so: size ${packed_size}B, expected ~${custom_size}B")
            result="SIZE MISMATCH"
        fi
        rows+=("| $abi | $so | $custom_size | $packed_size | ${custom:0:12}.. | ${packed:0:12}.. | $result |")
    done
done

{
    if [ "${#warnings[@]}" -gt 0 ]; then
        echo "## Custom build verification failed"
    else
        echo "## Custom build verification passed"
    fi
    echo ""
    echo "| ABI | Library | Size (maven-local, B) | Size (APK, B) | sha256 (maven-local) | sha256 (APK) | Result |"
    echo "| --- | --- | --- | --- | --- | --- | --- |"
    for r in "${rows[@]}"; do
        echo "$r"
    done
} >>"${GITHUB_STEP_SUMMARY:-/dev/null}"

if [ "${#warnings[@]}" -gt 0 ]; then
    for w in "${warnings[@]}"; do
        echo "::warning::$w"
    done
    # Non-zero exit marks the step yellow; the workflow step uses
    # continue-on-error so this never blocks artifact upload/release.
    exit 1
fi
exit 0
