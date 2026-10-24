# 更新记录

## 1.0.0（未发布）

- Maven、iOS Pod 与 OHPM 统一版本为 1.0.0；同仓模块使用同版，Kuikly 配套保持 2.29.1。
- 本地源码/产物消费与设备、远程发布验收分别记录；不覆盖旧标签或修改历史发布字节。

## 0.2.0-rc.2（RC 准备，尚未发布）

- 修复公开 Native 消费在复制 Podfile.lock 前未创建 `ci-diagnostics`，导致 rc.1 在 Pod/Renderer 构建成功后提前退出的问题；从不存在日志目录的 fixture 覆盖此顺序。
- Maven 两产品与 12 个 publication 不变；Native Git Pod 继续固定 `0.1.6`、Renderer `2.28.0`、HAR `0.1.3`。
- 使用新不可变 RC 重新冻结真实归档；保留 rc.1 tag、归档与 checksum。远程源码 CI、公开 Maven、Android / Native 消费待本轮新证据。

## 0.2.0-rc.1（已发布，Native 公开消费未完成）

- 删除 `toast-cmp` 公开坐标和 `io.github.gycrosskit.toast.cmp` API；保留 `toast` / `toast-kuikly`，共 12 个 Maven publication、KuiklyCompose / runtime 与 native 接线。
- Git Pod `GycToastNative/Kuikly` 固定 tag / Pod `0.1.6`；HAR 保持 `0.1.3`；不重发未修改的原生产物。
- 完整归档、冻结 checksum、远程源码 CI 与精确公开消费须由真实发布收据确认，历史 tag / checksum 保持不变。
- 迁移与验证边界见 [RC 迁移说明](docs/RC迁移说明.md)。
