#!/bin/bash

OUTPUT_ROOT="build/ci-connected-android-test-reports"
echo "reports-path=$OUTPUT_ROOT" >>"$GITHUB_OUTPUT"

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
    mkdir -p "$target_dir/app" "$target_dir/shared"

    cp -r app/build/reports/androidTests/connected/* "$target_dir/app/" 2>/dev/null || true
    cp -r shared/build/reports/androidTests/connected/* "$target_dir/shared/" 2>/dev/null || true
}

echo "=== Starting Portrait Tests ==="
set_orientation 0
if ! ./gradlew connectedDebugAndroidTest --stacktrace; then
    echo "❌ Portrait tests failed!"
    TEST_FAILED=1
fi
archive_reports "portrait"

echo "=== Starting Landscape Tests ==="
set_orientation 1
if ! ./gradlew connectedDebugAndroidTest --stacktrace; then
    echo "❌ Landscape tests failed!"
    TEST_FAILED=1
fi
archive_reports "landscape"

if [ $TEST_FAILED -ne 0 ]; then
    echo "❌ One or more test stages failed."
    exit 1
fi

echo "✅ All UI tests passed."
