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
        # If release is unsigned, Gradle installRelease might fail, so fallback to adb install if needed
        ./gradlew "$INSTALL_TASK" || adb install -r "$APK_PATH"
    else
        ./gradlew "$INSTALL_TASK"
    fi
    echo "==> Installation complete!"
fi
