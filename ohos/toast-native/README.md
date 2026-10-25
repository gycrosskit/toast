# @gycrosskit/toast-native

> 当前源码候选统一为 **1.0.1**：Maven（core/Kuikly）、iOS Pod 与 OHPM 使用同版；尚未发布，示例坐标须待发布后远程消费。本地验证使用显式源码/本地 HAR，Kuikly 基线为 2.29.1，真机与新版本远程消费另验。

新消息替换旧消息的 HarmonyOS 短消息展示。当前 HAR target/compatible SDK 为 HarmonyOS API 22。

```sh
ohpm install @gycrosskit/toast-native@1.0.1
```

HAR、Maven 与 Pod 的当前目标均为 1.0.1（未发布）。历史0.1.3轮次Registry查询/安装曾返回NOTFOUND；旧轮次仅文档更新且未重查 Registry，不能据旧记录声称当前可安装或仍审核。Release HAR下载/编译与Registry独立。共同KuiklyCompose Overlay不由此HAR绘制，功能/限制见[功能与平台差异](../../docs/功能与平台差异.md)。

```typescript
import { GycToastPresenter } from '@gycrosskit/toast-native';
GycToastPresenter.shared.show(context, '操作完成');
```

context 为主窗口已创建的 UIAbilityContext。文案由宿主本地化，不申请额外权限；无消息队列或交互。Kuikly 可注册 GycToastModule。openToast/closeToast 接口从 API 18 起提供，当前 HAR 的 compatible SDK 为 API 22。

[完整接入指南](https://github.com/gycrosskit/toast/blob/main/docs/接入指南.md) · [开发与验证](https://github.com/gycrosskit/toast/blob/main/docs/开发与验证.md) · [版本](https://github.com/gycrosskit/toast/releases) · [问题反馈](https://github.com/gycrosskit/toast/issues)。

Apache-2.0，见 [LICENSE](LICENSE)。
