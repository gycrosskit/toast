# Changelog

## Native/HAR 1.0.2

- iOS Git Pod 使用 `native-1.0.2`，HAR/OHPM 使用 `har-1.0.2`；Maven 保留 `1.0.1`，不重发未修改的 KMP 产物。
- Renderer 版本由宿主选择：Pod 声明 `>= 2.29.1, < 3.0`，HAR 声明 `^2.29.1`；范围不代替实际消费验证。
- 旧 Tag/归档/摘要保持不变；发布与精确公开消费状态见对应 Release，设备验收单独记录。

## 1.0.1（未发布）

- Maven、iOS Pod 与 OHPM 的本轮配套候选改为 1.0.1，保留已发布 1.0.0 的 Tag、归档和摘要。
- 以最新合并 dev 的准确 SHA 重新执行 verify、package、release 和公开消费；历史收据不作为本轮通过。


## 1.0.0（历史准备与部分发布）

- Maven、iOS Pod 与 OHPM 统一版本为 1.0.0；同仓模块使用同版，Kuikly 配套保持 2.29.1。
- 本地源码/产物消费与设备、远程发布验收分别记录；不覆盖旧标签或修改历史发布字节。

- 完成本轮既有能力边界迁移，具体 API 与兼容规则见 README。

## 0.1.2

- Align the HAR version with the Toast 0.1.2 release. No native API changes.

## 0.1.0

- Initial native presenter and Kuikly bridge.
