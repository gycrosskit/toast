#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${VERSION:?Provide an immutable release tag}"
[[ "$VERSION" =~ ^[0-9][0-9A-Za-z._-]*$ ]] || { echo 'Invalid release tag' >&2; exit 1; }
staging="$(mktemp -d)"
preserve_evidence() {
  status=$?
  diagnostics="${CI_DIAGNOSTICS_DIR:-ci-diagnostics}/public"
  mkdir -p "$diagnostics"
  printf 'version=%s\nexit=%s\n' "$VERSION" "$status" > "$diagnostics/result.txt"
  rm -rf "$staging"
  exit "$status"
}
trap preserve_evidence EXIT
# 从不可变版本 Tag 取得提交和 tree，沿用 JitPack 的回执与归档校验。
repo_root="$PWD"
git init -q "$staging/tag"
git -C "$staging/tag" fetch -q --no-tags --depth=1 https://github.com/gycrosskit/toast.git "refs/tags/$VERSION"
git -C "$staging/tag" checkout -q --detach FETCH_HEAD
commit="$(git -C "$staging/tag" rev-parse --verify HEAD)"
(cd "$staging/tag" && bash "$repo_root/jitpack-install.sh" toast "$staging/maven")
python3 scripts/check-maven.py "$staging/maven" com.github.gycrosskit.toast "$VERSION" toast,toast-kuikly ios_arm64,ios_x64,ios_simulator_arm64,ohos_arm64
# 标签必须解析为不可变发布提交；不把当前 PR 的 SHA 当成已发布版本。
publications=toast,toast-android,toast-iosarm64,toast-iosx64,toast-iossimulatorarm64,toast-ohosarm64,toast-kuikly,toast-kuikly-android,toast-kuikly-iosarm64,toast-kuikly-iosx64,toast-kuikly-iossimulatorarm64,toast-kuikly-ohosarm64
python3 scripts/check-public-maven.py --repo toast --version "$VERSION" --commit "$commit" \
  --expected-publications "$publications" --output-dir "${CI_DIAGNOSTICS_DIR:-ci-diagnostics}/public"
