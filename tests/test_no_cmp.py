"""源码边界与发布清单回归；不替代 Gradle 依赖解析或 Renderer 验收。"""
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
from tempfile import TemporaryDirectory
import unittest

ROOT = Path(__file__).resolve().parents[1]
GROUP = "com.github.gycrosskit.toast"
VERSION = "1.0.0"
PRODUCTS = ("toast", "toast-kuikly")
TARGETS = {"android": None, "iosarm64": "ios_arm64", "iosx64": "ios_x64",
           "iossimulatorarm64": "ios_simulator_arm64", "ohosarm64": "ohos_arm64"}


class NoCmpContract(unittest.TestCase):
    def test_production_keeps_kuikly_compose_and_shared_core(self):
        self.assertFalse(list((ROOT / "toast-cmp").rglob("*.kt")))
        self.assertNotIn(':toast-cmp', (ROOT / "settings.gradle.kts").read_text())
        version = re.search(r'environmentVariable\("VERSION"\)\.orElse\("([^"\n]+)"\)', (ROOT / 'build.gradle.kts').read_text()).group(1)
        self.assertIn('gradleProperty("toastVersion").orElse("' + version + '")', (ROOT / 'verification/build.gradle.kts').read_text())
        self.assertIn("spec.version = '" + version + "'", (ROOT / 'GycToastNative.podspec').read_text())
        self.assertEqual(version, json.loads((ROOT / 'ohos/toast-native/oh-package.json5').read_text())['version'])
        catalog = (ROOT / "gradle/libs.versions.toml").read_text()
        kuikly = re.search(r'^kuikly = "([^"]+)"$', catalog, re.M).group(1)
        self.assertEqual('2.29.1-2.0.21-ohos', kuikly)
        renderer = kuikly.split('-', 1)[0]
        self.assertIn("kuikly.dependency 'OpenKuiklyIOSRender', '" + renderer + "'", (ROOT / 'GycToastNative.podspec').read_text())
        self.assertIn("pod 'OpenKuiklyIOSRender', '" + renderer + "'", (ROOT / 'scripts/ci-build-kuikly-render.sh').read_text())
        self.assertEqual(renderer, json.loads((ROOT / 'ohos/toast-native/oh-package.json5').read_text())['dependencies']['@kuikly-open/render'])
        self.assertNotIn('org.jetbrains.compose', catalog)
        self.assertIn('com.tencent.kuikly-open:compose', catalog)
        self.assertIn('org.jetbrains.kotlin.plugin.compose', catalog)
        for module in ("toast-core", "toast-kuikly"):
            for path in (ROOT / module / "src").rglob("*.kt"):
                self.assertNotRegex(path.read_text(), r'import (?:androidx\.compose\.(?:ui|foundation|material)|io\.github\.gycrosskit\.toast\.cmp)')
        self.assertIn('MessageOverlayState', (ROOT / "toast-core/src/commonMain/kotlin/io/github/gycrosskit/toast/MessageOverlayState.kt").read_text())
        host = (ROOT / "toast-kuikly/src/commonMain/kotlin/io/github/gycrosskit/toast/kuikly/MessageHost.kt").read_text()
        self.assertIn('com.tencent.kuikly.compose.ui.Modifier', host)
        self.assertIn('androidx.compose.runtime.Composable', host)

    def test_consumer_and_ci_require_isolation(self):
        consumer = (ROOT / "verification/build.gradle.kts").read_text()
        self.assertNotIn('toast-cmp', consumer.split('tasks.register("verifyNoCmpUi")')[0])
        self.assertNotIn('verifyCmp', consumer)
        self.assertNotIn('kotlin.exclude', consumer)
        self.assertIn('tasks.register("verifyNoCmpUi")', consumer)
        self.assertIn('debugRuntimeClasspath', consumer)
        self.assertIn('resolution.allDependencies.filterIsInstance<org.gradle.api.artifacts.result.UnresolvedDependencyResult>()', consumer)
        self.assertLess(consumer.index('check(unresolved.isEmpty())'), consumer.index('val components = resolution.allComponents'))
        self.assertIn('listOf("ui", "foundation", "material")', consumer)
        self.assertIn('com.tencent.kuikly-open', consumer)
        self.assertIn('it.version == toastVersion', consumer)
        settings = (ROOT / "verification/settings.gradle.kts").read_text()
        self.assertIn('if (providers.gradleProperty("verifyLocalSource").isPresent)', settings)
        self.assertIn('substitute(module("com.github.gycrosskit.toast:toast")).using(project(":toast"))', settings)
        self.assertIn('substitute(module("com.github.gycrosskit.toast:toast-kuikly")).using(project(":toast-kuikly"))', settings)
        self.assertNotIn('mavenLocal', settings)
        self.assertIn('ProjectComponentIdentifier', consumer)
        for path in (ROOT / "verification/src").rglob("*.kt"):
            self.assertNotIn('toast.cmp', path.read_text())

    def test_publication_checker_accepts_two_products_and_rejects_retired_product(self):
        release = (ROOT / "scripts/ci-release.sh").read_text()
        publications = re.search(r'^publications=(\S+)$', release, re.M).group(1).split(',')
        expected = {name for product in PRODUCTS for name in (product, *(f'{product}-{target}' for target in TARGETS))}
        self.assertEqual(set(publications), expected)
        self.assertIn('"$VERSION" toast,toast-kuikly ', release)
        with TemporaryDirectory() as folder:
            repository = Path(folder)
            for product in PRODUCTS:
                variants = []
                for suffix, target in TARGETS.items():
                    name = f'{product}-{suffix}'
                    variant_name = f'{suffix}ApiElements'
                    variant = {"name": variant_name}
                    if target:
                        variant["attributes"] = {"org.jetbrains.kotlin.native.target": target}
                    self.write_module(repository, name, [variant], '.aar' if suffix == 'android' else '.klib')
                    variants.append({"name": variant_name, "available-at": {
                        "group": GROUP, "module": name, "version": VERSION,
                        "url": f'../../{name}/{VERSION}/{name}-{VERSION}.module'}})
                self.write_module(repository, product, variants, '.jar')
            command = [sys.executable, str(ROOT / "scripts/check-maven.py"), folder, GROUP, VERSION,
                       ','.join(PRODUCTS), ','.join(target for target in TARGETS.values() if target)]
            result = subprocess.run(command, capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
            checksum = next(repository.rglob('*.pom.sha512'))
            saved = checksum.read_bytes()
            checksum.unlink()
            result = subprocess.run(command, capture_output=True, text=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn('Missing checksum', result.stderr)
            checksum.write_bytes(saved)
            self.write_module(repository, 'toast-cmp', [], '.jar')
            result = subprocess.run(command, capture_output=True, text=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn('Missing or unexpected publications', result.stderr)

    def write_module(self, repository, name, variants, extension):
        directory = repository / GROUP.replace('.', '/') / name / VERSION
        directory.mkdir(parents=True)
        artifact = directory / f'{name}-{VERSION}{extension}'
        artifact.write_bytes(b'fixture')
        if not variants:
            variants = [{"name": "metadataApiElements"}]
        variants[0]["files"] = [{"url": artifact.name, "size": artifact.stat().st_size,
            **{algorithm: hashlib.new(algorithm, artifact.read_bytes()).hexdigest()
               for algorithm in ('md5', 'sha1', 'sha256', 'sha512')}}]
        (directory / f'{name}-{VERSION}.module').write_text(json.dumps({
            "component": {"group": GROUP, "module": name, "version": VERSION}, "variants": variants}))
        (directory / f'{name}-{VERSION}.pom').write_text(f'''<project xmlns="http://maven.apache.org/POM/4.0.0">
<groupId>{GROUP}</groupId><artifactId>{name}</artifactId><version>{VERSION}</version>
<licenses><license><name>Apache License, Version 2.0</name>
<url>https://www.apache.org/licenses/LICENSE-2.0.txt</url><distribution>repo</distribution></license></licenses></project>''')
        for file in directory.iterdir():
            if file.suffix in ('.pom', '.module', '.aar', '.klib', '.jar'):
                for algorithm in ('md5', 'sha1', 'sha256', 'sha512'):
                    file.with_name(file.name + '.' + algorithm).write_text(hashlib.new(algorithm, file.read_bytes()).hexdigest())


if __name__ == "__main__":
    unittest.main()
