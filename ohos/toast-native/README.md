# @gycrosskit/toast-native

新消息替换旧消息的 HarmonyOS 短消息展示。当前 HAR target/compatible SDK 为 HarmonyOS API 22。

```sh
ohpm install @gycrosskit/toast-native@0.1.3
```

0.1.3 为候选，OHPM next 提交已接受、仍在审核；精确版本 Registry 查询/安装曾返回 NOTFOUND。审核通过并可查询后再运行上述安装命令，稳定 latest 保留 0.1.2。Release HAR 的下载与编译状态独立记录在仓库 README。

```typescript
import { GycToastPresenter } from '@gycrosskit/toast-native';
GycToastPresenter.shared.show(context, '操作完成');
```

context 为主窗口已创建的 UIAbilityContext。文案由宿主本地化，不申请额外权限；无消息队列或交互。Kuikly 可注册 GycToastModule。openToast/closeToast 接口从 API 18 起提供，当前 HAR 的 compatible SDK 为 API 22。

[完整接入指南](https://github.com/gycrosskit/toast/blob/main/docs/接入指南.md) · [开发与验证](https://github.com/gycrosskit/toast/blob/main/docs/开发与验证.md) · [版本](https://github.com/gycrosskit/toast/releases) · [问题反馈](https://github.com/gycrosskit/toast/issues)。

Apache-2.0，见 [LICENSE](LICENSE)。
