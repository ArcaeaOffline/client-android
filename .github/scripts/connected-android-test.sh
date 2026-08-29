#!/bin/bash
set -euo pipefail

OUTPUT_ROOT="build/ci-connected-android-test-reports"
echo "reports-path=$OUTPUT_ROOT" >>"$GITHUB_OUTPUT"

# NOTE: on API 24 the app tests log "additionalTestOutput is not supported
# on this device running API level 24" — the OCR additional-test-output
# directory is unavailable on that image. Harmless; tests still pass.

# Keep in sync with the "Build Application" step in
# .github/workflows/connected-android-test.yml
TEST_TASKS="app:connectedUnstableDebugAndroidTest core:connectedDebugAndroidTest shared:connectedAndroidDeviceTest"

TEST_FAILED=0

set_orientation() {
    # portrait: 0, landscape: 1
    local rotation=$1
    adb shell settings put system accelerometer_rotation 0
    adb shell settings put system user_rotation "$rotation"
}

archive_reports() {
    local stage=$1
    local target_dir="$OUTPUT_ROOT/$stage"

    echo "=== Archiving $stage test reports ==="

    # Every module listed in TEST_TASKS is expected to produce a connected
    # test report dir. A missing/empty one means the gradle run claimed
    # success without discoverable results — fail loudly instead of
    # silently uploading an empty artifact (which actually happened before,
    # see commit ffa831a).
    local module src
    for module in app core shared; do
        src="$module/build/reports/androidTests/connected"
        if [ ! -d "$src" ] || [ -z "$(ls -A "$src")" ]; then
            echo "❌ No connected test reports found for :$module: (expected at $src)"
            exit 1
        fi
        mkdir -p "$target_dir/$module"
        cp -r "$src"/* "$target_dir/$module/"
    done
}

echo "=== Starting Portrait Tests ==="
set_orientation 0
if ! ./gradlew $TEST_TASKS --stacktrace; then
    echo "❌ Portrait tests failed!"
    TEST_FAILED=1
fi
archive_reports "portrait"

echo "=== Starting Landscape Tests ==="
set_orientation 1
if ! ./gradlew $TEST_TASKS --stacktrace; then
    echo "❌ Landscape tests failed!"
    TEST_FAILED=1
fi
archive_reports "landscape"

if [ $TEST_FAILED -ne 0 ]; then
    echo "❌ One or more test stages failed."
    exit 1
fi

echo "✅ All UI tests passed."
