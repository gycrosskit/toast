const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const ts = require(process.env.TYPESCRIPT_PATH || '/Applications/DevEco-Studio.app/Contents/tools/hvigor/hvigor/node_modules/typescript');

function load(name, dependencies) {
  const source = fs.readFileSync(`${__dirname}/../ohos/toast-native/src/main/ets/${name}.ets`, 'utf8');
  const output = ts.transpileModule(source, { compilerOptions: {
    module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020
  } }).outputText;
  const exports = {};
  vm.runInNewContext(output, { exports, require: key => dependencies[key] || {} });
  return exports;
}

(async () => {
  const opens = [], closes = [];
  const prompt = {
    openToast(options) {
      let finish;
      const result = new Promise(resolve => { finish = resolve; });
      opens.push({ options, finish });
      return result;
    },
    closeToast(id) { closes.push(id); }
  };
  const context = { windowStage: { getMainWindowSync: () => ({
    getUIContext: () => ({ getPromptAction: () => prompt })
  }) } };
  const { GycToastPresenter } = load('GycToastPresenter', {});
  let destroyed = 0;
  const { GycToastModule } = load('GycToastModule', {
    './GycToastPresenter': { GycToastPresenter },
    '@kuikly-open/render': { KuiklyRenderBaseModule: class {
      controller = { getUIAbilityContext: () => context };
      onDestroy() { destroyed++; }
    } }
  });
  const flush = async () => { for (let i = 0; i < 12; i++) await Promise.resolve(); };
  const show = (module, message, duration = 'SHORT') => module.call('show', JSON.stringify({ message, duration }), null);
  const a = new GycToastModule(), b = new GycToastModule();
  a.call('show', '{bad', null);
  show(a, 'ignored', 'INVALID');
  show(a, '   ');
  await flush();
  assert.equal(opens.length, 0);

  show(a, 'first');
  await flush();
  assert.equal(opens.length, 1);
  assert.equal(opens[0].options.duration, 2000);
  show(b, ' second ', 'LONG');
  a.onDestroy();
  show(a, 'late old-page message');
  opens[0].finish(1);
  await flush();
  assert.deepEqual(closes, [1], 'late old open must close before replacement');
  assert.equal(opens.length, 2, 'destroyed old module cannot replace the new page');
  assert.equal(opens[1].options.message, 'second');
  assert.equal(opens[1].options.duration, 3500);
  opens[1].finish(2);
  await flush();
  assert.deepEqual(closes, [1], 'old-page destroy must not close the new toast');
  b.onDestroy();
  show(b, 'late');
  await flush();
  assert.equal(opens.length, 2);
  assert.equal(destroyed, 2);

  const unavailable = new GycToastModule();
  unavailable.controller = null;
  show(unavailable, 'no context');
  await flush();
  assert.equal(opens.length, 2);
  console.log('Toast bridge/presenter: malformed input, durations, late open replacement and destroyed-page ownership passed');
})().catch(error => { console.error(error); process.exitCode = 1; });
