#!/bin/bash
set -e

# build.sh - Build and install helper script for Yomu
# Usage:
#   ./build.sh debug
#   ./build.sh debug --install
#   ./build.sh release
#   ./build.sh release --install

BUILD_TYPE="debug"
INSTALL=false

for arg in "$@"; do
    case "$arg" in
        debug)
            BUILD_TYPE="debug"
            ;;
        release)
            BUILD_TYPE="release"
            ;;
        --install|-i)
            INSTALL=true
            ;;
        *)
            echo "Unknown option: $arg"
            echo "Usage: $0 [debug|release] [--install]"
            exit 1
            ;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Ensure git submodule is initialized
if [ ! -f "ml/llama.cpp/CMakeLists.txt" ]; then
    echo "==> Initializing ml/llama.cpp submodule..."
    git submodule update --init --recursive
fi

# Reuse the shared local release key (same as y2player) when YOMU_* is not set.
if [ -z "$YOMU_KEYSTORE_FILE" ] && [ -f "$Y2_KEYSTORE_PATH" ]; then
    export YOMU_KEYSTORE_FILE="$Y2_KEYSTORE_PATH"
    export YOMU_KEYSTORE_PASSWORD="$Y2_STORE_PASS"
    export YOMU_KEY_ALIAS="$Y2_KEY_ALIAS"
    export YOMU_KEY_PASSWORD="$Y2_KEY_PASS"
fi

if [ "$BUILD_TYPE" = "release" ]; then
    TASK="assembleRelease"
    INSTALL_TASK="installRelease"
    APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
else
    TASK="assembleDebug"
    INSTALL_TASK="installDebug"
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
fi

echo "==> Building $BUILD_TYPE ($TASK)..."
./gradlew "$TASK"

if [ "$INSTALL" = true ]; then
    echo "==> Installing $BUILD_TYPE to connected device..."
    if [ "$BUILD_TYPE" = "release" ]; then
        if [ -n "$YOMU_KEYSTORE_FILE" ]; then
            ./gradlew "$INSTALL_TASK"
        else
            # No release keystore: sign the unsigned APK with the local debug key so adb accepts it.
            SDK_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null)}}"
            APKSIGNER="$(ls -d "$SDK_DIR"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -1)"
            [ -x "$APKSIGNER" ] || { echo "ERROR: apksigner not found (SDK: $SDK_DIR)"; exit 1; }
            SIGNED_APK="app/build/outputs/apk/release/app-release-debugsigned.apk"
            echo "==> No YOMU_KEYSTORE_FILE; signing with ~/.android/debug.keystore"
            "$APKSIGNER" sign --ks "$HOME/.android/debug.keystore" --ks-pass pass:android \
                --ks-key-alias androiddebugkey --key-pass pass:android --out "$SIGNED_APK" "$APK_PATH"
            adb install -r "$SIGNED_APK"
        fi
    else
        ./gradlew "$INSTALL_TASK"
    fi
    echo "==> Installation complete!"
fi
