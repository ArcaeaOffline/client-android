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

# Environment-instability signatures seen on older API images (e.g. API 24):
# the emulator becomes unresponsive mid-APK-install. When a failure matches
# one of these we retry ONCE after restarting adb. Anything else is treated
# as a real regression and must not be retried.
RETRY_SIGNATURES=(
    "ShellCommandUnresponsiveException"
    "InstallException"
    "install-write"
    "device offline"
    "device not found"
    # Rarer variant of the same stall: the activity fails to launch on a
    # device that hung mid-install, and Compose tests report it as
    # "No compose hierarchies found".
    "No compose hierarchies found"
)

TEST_FAILED=0

set_orientation() {
    # portrait: 0, landscape: 1
    local rotation=$1
    adb shell settings put system accelerometer_rotation 0
    adb shell settings put system user_rotation "$rotation"
}

is_environment_failure() {
    local log=$1 signature
    for signature in "${RETRY_SIGNATURES[@]}"; do
        if grep -q "$signature" "$log"; then
            return 0
        fi
    done
    return 1
}

dump_failure_diagnostics() {
    local out="$OUTPUT_ROOT/diagnostics"
    mkdir -p "$out"
    echo "=== Dumping failure diagnostics to $out ==="
    # The device may be half-dead at this point, so tolerate dump failures.
    timeout 30 adb logcat -d >"$out/logcat.log" 2>&1 || echo "⚠️ logcat dump failed or timed out"
    adb devices -l >"$out/devices.txt" 2>&1 || true
}

run_stage() {
    local stage=$1 orientation=$2 attempt
    local log
    mkdir -p "$OUTPUT_ROOT"
    set_orientation "$orientation"

    for attempt in 1 2; do
        log="$OUTPUT_ROOT/gradle-$stage-attempt$attempt.log"
        if ./gradlew $TEST_TASKS --stacktrace 2>&1 | tee "$log"; then
            return 0
        fi
        if [ "$attempt" -eq 1 ] && is_environment_failure "$log"; then
            echo "⚠️ Environment failure detected (device unresponsive during install?), retrying once"
            dump_failure_diagnostics
            adb kill-server || true
            adb start-server || true
            adb wait-for-device || true
        else
            return 1
        fi
    done
    return 1
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
if ! run_stage portrait 0; then
    echo "❌ Portrait tests failed!"
    TEST_FAILED=1
fi
archive_reports "portrait"

echo "=== Starting Landscape Tests ==="
if ! run_stage landscape 1; then
    echo "❌ Landscape tests failed!"
    TEST_FAILED=1
fi
archive_reports "landscape"

if [ $TEST_FAILED -ne 0 ]; then
    echo "❌ One or more test stages failed."
    exit 1
fi

echo "✅ All UI tests passed."
