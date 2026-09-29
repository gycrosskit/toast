# @gycrosskit/toast-native

HarmonyOS 原生 Toast 展示组件，新消息会替换旧消息。原生 ArkTS 可调用导出的 `GycToastPresenter`；`GycToastModule` 用于对接 Kuikly。

以下安装命令需在目标版本通过 ohpm 审核并可从仓库查询后使用。上架状态以 ohpm 查询结果为准。

```sh
ohpm install @gycrosskit/toast-native
```

```ts
import { GycToastModule } from '@gycrosskit/toast-native';
modules.set(GycToastModule.MODULE_NAME, () => new GycToastModule());
```

采用 Apache-2.0 许可证。源码：[gycrosskit/toast](https://github.com/gycrosskit/toast)。
