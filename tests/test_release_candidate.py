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

print('PASS: 12-publication package has real SHA; missing version/sidecar fail. Fixture only, no SDK/network.')
