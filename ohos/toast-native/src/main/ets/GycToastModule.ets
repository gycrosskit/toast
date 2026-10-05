import { KRAny, KuiklyRenderBaseModule, KuiklyRenderCallback } from '@kuikly-open/render';
import { GycToastPresenter } from './GycToastPresenter';

/** 每页的 Kuikly 提示桥；只转发 show，销毁后忽略请求，原生展示归 presenter 所有。 */
export class GycToastModule extends KuiklyRenderBaseModule {
  /** 原生注册名，与 Kotlin ToastModule.NAME 一致。 */
  static readonly MODULE_NAME: string = 'GycToastModule';
  private destroyed: boolean = false;

  syncMode(): boolean { return false; }

  call(method: string, params: KRAny, _callback: KuiklyRenderCallback | null): KRAny {
    if (this.destroyed) return null;
    if (method !== 'show' || typeof params !== 'string') return null;
    const context = this.controller?.getUIAbilityContext();
    if (!context) return null;
    try {
      const args: Record<string, string> = JSON.parse(params as string);
      if (args.message && (args.duration === 'SHORT' || args.duration === 'LONG')) {
        GycToastPresenter.shared.show(context, args.message, args.duration === 'LONG');
      }
    } catch (_) { /* Ignore malformed bridge input. */ }
    return null;
  }

  onDestroy(): void {
    this.destroyed = true;
    super.onDestroy();
  }
}
