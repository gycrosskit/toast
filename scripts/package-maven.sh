#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${VERSION:?Pass the immutable candidate version; this script does not upload or tag}"
[[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][A-Za-z0-9.-]+)?$ ]] || { echo 'Invalid release version' >&2; exit 1; }
export GROUP=com.github.gycrosskit.toast
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
# staging 为本脚本独占的生成仓库，避免混入旧版本。
rm -rf build/maven
bash gradlew "$@" --no-daemon --max-workers=1 -Dorg.gradle.parallel=false :toast:publishAllPublicationsToStagingRepository :toast-kuikly:publishAllPublicationsToStagingRepository
python3 scripts/jitpack-metadata.py build/maven/com/github/gycrosskit/toast
python3 scripts/check-maven.py build/maven "$GROUP" "$VERSION" toast,toast-kuikly ios_arm64,ios_x64,ios_simulator_arm64,ohos_arm64 toast,toast-android,toast-iosarm64,toast-iosx64,toast-iossimulatorarm64,toast-ohosarm64,toast-kuikly,toast-kuikly-android,toast-kuikly-iosarm64,toast-kuikly-iosx64,toast-kuikly-iossimulatorarm64,toast-kuikly-ohosarm64
COPYFILE_DISABLE=1 tar --no-xattrs -czf build/toast-maven.tar.gz -C build/maven com
(cd build && shasum -a 256 toast-maven.tar.gz > toast-maven.tar.gz.sha256 && cat toast-maven.tar.gz.sha256)
