"""Run candidate packaging and retained Pod identity gates using fixture bytes only."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import tarfile
from tempfile import TemporaryDirectory

ROOT = Path(__file__).resolve().parents[1]
workflow = json.loads(subprocess.check_output([
    'ruby', '-ryaml', '-rjson', '-e',
    'puts JSON.generate(YAML.safe_load(File.read(ARGV.fetch(0))))',
    str(ROOT / '.github/workflows/regression.yml'),
], text=True))
assert workflow['jobs']['release-native']['env']['NATIVE_VERSION'] == '1.0.0'
for job in ('release-android', 'release-native'):
    consumers = [step['run'] for step in workflow['jobs'][job]['steps']
                 if '-p verification' in step.get('run', '')]
    assert consumers and all('-PremoteOnly' in command and '-PverifyLocalSource' not in command
                             for command in consumers)
native = next(step['run'] for step in workflow['jobs']['release-native']['steps']
              if 'render_dir=' in step.get('run', ''))
assert 'GycToastNative/Kuikly toast "$NATIVE_VERSION"' in native
assert native.index('mkdir -p ci-diagnostics') < native.index('cp "$RUNNER_TEMP/kuikly-ios-render/Podfile.lock"')
receipt = native[native.index('mkdir -p ci-diagnostics'):native.index('python3 scripts/ci-run.py')]

with TemporaryDirectory() as folder:
    root = Path(folder)
    (root / 'scripts').mkdir()
    for name in ('package-maven.sh', 'check-maven.py', 'jitpack-metadata.py'):
        shutil.copyfile(ROOT / 'scripts' / name, root / 'scripts' / name)
    (root / 'gradlew').write_text('''#!/usr/bin/env bash
set -euo pipefail
python3 - "$FIXTURE_CONTRACT" <<'FIXTURE'
import os, runpy, sys
from pathlib import Path
contract = runpy.run_path(sys.argv[1])
fixture = contract['NoCmpContract']()
repo = Path('build/maven')
for product in contract['PRODUCTS']:
    variants = []
    for suffix, target in contract['TARGETS'].items():
        name = f'{product}-{suffix}'
        variant = {'name': f'{suffix}ApiElements'}
        if target:
            variant['attributes'] = {'org.jetbrains.kotlin.native.target': target}
        fixture.write_module(repo, name, [variant], '.aar' if suffix == 'android' else '.klib')
        variants.append({'name': variant['name'], 'available-at': {
            'group': contract['GROUP'], 'module': name, 'version': contract['VERSION'],
            'url': f"../../{name}/{contract['VERSION']}/{name}-{contract['VERSION']}.module"}})
    fixture.write_module(repo, product, variants, '.jar')
if os.environ.get('FIXTURE_MISSING_SIDECAR') == '1':
    next(repo.rglob('*.pom.sha512')).unlink()
FIXTURE
''')
    environment = dict(os.environ, VERSION='1.0.0', FIXTURE_CONTRACT=str(ROOT / 'tests/test_no_cmp.py'))
    command = ['bash', 'scripts/package-maven.sh']
    result = subprocess.run(command, cwd=root, env=environment, capture_output=True, text=True)
    assert result.returncode == 0, result.stdout + result.stderr
    archive = root / 'build/toast-maven.tar.gz'
    checksum = root / 'build/toast-maven.tar.gz.sha256'
    assert checksum.read_text().split()[0] == hashlib.sha256(archive.read_bytes()).hexdigest()
    with tarfile.open(archive) as bundle:
        assert not any(Path(item.name).name.startswith('._') for item in bundle)
        assert len([item for item in bundle if item.name.endswith('.module')]) == 12
    archive.unlink()
    checksum.unlink()
    result = subprocess.run(command, cwd=root, env=dict(environment, FIXTURE_MISSING_SIDECAR='1'), capture_output=True, text=True)
    assert result.returncode != 0 and 'Missing checksum' in result.stderr
    assert not archive.exists() and not checksum.exists()
    environment.pop('VERSION')
    result = subprocess.run(command, cwd=root, env=environment, capture_output=True, text=True)
    assert result.returncode != 0 and 'immutable candidate' in result.stderr

with TemporaryDirectory() as folder:
    root = Path(folder)
    installed = root / 'kuikly-ios-render/Pods/GycToastNative/iosApp/Sources/GycToastNative'
    installed.mkdir(parents=True)
    (installed / 'Toast.swift').write_text('fixture native Swift')
    tools = root / 'tools'
    tools.mkdir()
    git = tools / 'git'
    git.write_text('''#!/usr/bin/env bash
set -euo pipefail
case "$1" in
  clone) dest="${!#}"; mkdir -p "$dest/iosApp/Sources"; cp -R "$RUNNER_TEMP/kuikly-ios-render/Pods/GycToastNative/iosApp/Sources/GycToastNative" "$dest/iosApp/Sources/"; test "${BAD_NATIVE_BYTES:-0}" = 0 || printf changed > "$dest/iosApp/Sources/GycToastNative/Toast.swift" ;;
  -C) printf '%s\\n' "$MOCK_HEAD" ;;
  ls-remote) printf '%s\\trefs/tags/%s\\n' "$MOCK_TAG_SHA" "$NATIVE_VERSION" ;;
  *) exit 1 ;;
esac
''')
    git.chmod(0o755)
    env = dict(os.environ, PATH=str(tools) + os.pathsep + os.environ['PATH'], RUNNER_TEMP=str(root),
               VERSION='1.0.0', NATIVE_VERSION='1.0.0', MOCK_HEAD='a' * 40, MOCK_TAG_SHA='a' * 40)
    for tag, version, head, changed, expected in [
        ('1.0.0', '1.0.0', 'a' * 40, '0', True),
        ('0.9.9', '1.0.0', 'a' * 40, '0', False),
        ('1.0.0', '0.9.9', 'a' * 40, '0', False),
        ('1.0.0', '1.0.0', 'b' * 40, '0', False),
        ('1.0.0', '1.0.0', 'a' * 40, '1', False),
    ]:
        (root / 'kuikly-ios-render/Podfile.lock').write_text(f'''PODS:
  - GycToastNative/Core ({version})
  - GycToastNative/Kuikly ({version}):
    - GycToastNative/Core (= {version})
CHECKOUT OPTIONS:
  GycToastNative:
    :tag: {tag}
    :git: https://github.com/gycrosskit/toast.git
''')
        shutil.rmtree(root / 'ci-diagnostics', ignore_errors=True)
        assert not (root / 'ci-diagnostics').exists()
        proof = root / 'ci-diagnostics/release-pod.json'
        result = subprocess.run(['bash', '-euo', 'pipefail', '-c', receipt], cwd=root,
                                env=dict(env, MOCK_HEAD=head, BAD_NATIVE_BYTES=changed), capture_output=True, text=True)
        assert (result.returncode == 0) == expected, result.stderr
        assert proof.exists() == expected

print('PASS: 12-publication package has real SHA; missing version/sidecar fail; fresh diagnostics directory created; retained Pod tag/version/SHA/bytes checked. Fixture only, no SDK/network.')
