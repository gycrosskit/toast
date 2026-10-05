import { common } from '@kit.AbilityKit';
import { PromptAction } from '@kit.ArkUI';

/** 进程共用 presenter；UIAbility 主线程调用，串行关闭旧提示并展示最新请求。 */
export class GycToastPresenter {
  /** 进程唯一入口，CMP 与 Kuikly 共用。 */
  static readonly shared: GycToastPresenter = new GycToastPresenter();

  private generation: number = 0;
  private activeId: number | null = null;
  private activePrompt: PromptAction | null = null;
  private pending: Promise<void> = Promise.resolve();

  /**
   * 空白文案忽略；open/close 失败不阻塞后续请求。
   * @param context 当前 UIAbility；通过其 Window 的 PromptAction 展示。
   * @param message 已本地化文本，首尾空白会去除。
   * @param longDuration 默认 false，短/长时长分别为 2000/3500 毫秒。
   */
  show(context: common.UIAbilityContext, message: string, longDuration: boolean = false): void {
    const text = message.trim();
    if (!text) return;
    const request = ++this.generation;
    // Serialize open/close so a late openToast result cannot outlive a newer request.
    this.pending = this.pending.catch(() => {}).then(async () => {
      if (request !== this.generation) return;
      const prompt = context.windowStage.getMainWindowSync().getUIContext().getPromptAction();
      if (this.activeId !== null && this.activePrompt !== null) {
        try { this.activePrompt.closeToast(this.activeId); } catch (_) { /* already expired */ }
        this.activeId = null;
        this.activePrompt = null;
      }
      if (request !== this.generation) return;
      const id = await prompt.openToast({ message: text, duration: longDuration ? 3500 : 2000 });
      if (request !== this.generation) {
        try { prompt.closeToast(id); } catch (_) { /* already expired */ }
        return;
      }
      this.activeId = id;
      this.activePrompt = prompt;
    }).catch(() => {});
  }
}
