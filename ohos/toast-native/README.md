# @gycrosskit/toast-native

HarmonyOS native Toast presenter with latest-message replacement. Exported `GycToastPresenter` can be called by native ArkTS; `GycToastModule` bridges Kuikly.

```sh
ohpm install @gycrosskit/toast-native
```

```ts
import { GycToastModule } from '@gycrosskit/toast-native';
modules.set(GycToastModule.MODULE_NAME, () => new GycToastModule());
```

Apache-2.0. Source: https://github.com/gycrosskit/toast
