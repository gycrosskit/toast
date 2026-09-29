import { common } from '@kit.AbilityKit';
import { PromptAction } from '@kit.ArkUI';

/** One presenter per app process; a later request replaces the current toast. */
export class GycToastPresenter {
  static readonly shared: GycToastPresenter = new GycToastPresenter();

  private generation: number = 0;
  private activeId: number | null = null;
  private activePrompt: PromptAction | null = null;
  private pending: Promise<void> = Promise.resolve();

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
