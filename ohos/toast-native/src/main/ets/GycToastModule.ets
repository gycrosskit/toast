import { KRAny, KuiklyRenderBaseModule, KuiklyRenderCallback } from '@kuikly-open/render';
import { GycToastPresenter } from './GycToastPresenter';

/** Kuikly transport only; native presentation is owned by GycToastPresenter. */
export class GycToastModule extends KuiklyRenderBaseModule {
  static readonly MODULE_NAME: string = 'GycToastModule';

  syncMode(): boolean { return false; }

  call(method: string, params: KRAny, _callback: KuiklyRenderCallback | null): KRAny {
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
}
