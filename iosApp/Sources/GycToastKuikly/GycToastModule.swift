import Foundation
import OpenKuiklyIOSRender
import UIKit

/// Objective-C 名称与 common Module 一致，由当前 Renderer 的 SDK 自动创建。
@objc(GycToastModule)
public final class GycToastModule: KRBaseModule {
    private let lifecycleLock = NSLock()
    private var invalidated = false

    @objc(show:)
    public func show(_ args: [String: Any]) {
        guard let params = args[KR_PARAM_KEY] as? String,
              let data = params.data(using: .utf8),
              let values = (try? JSONSerialization.jsonObject(with: data)) as? [String: Any],
              let message = values["message"] as? String,
              let duration = values["duration"] as? String,
              duration == "SHORT" || duration == "LONG",
              !message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        let display = { [weak self] in
            guard let self else { return }
            self.lifecycleLock.lock()
            let active = !self.invalidated
            self.lifecycleLock.unlock()
            guard active, self.hr_rootView != nil else { return }
            GycToastPresenter.shared.show(message: message, longDuration: duration == "LONG")
        }
        if Thread.isMainThread { display() } else { DispatchQueue.main.async(execute: display) }
    }

    public override func invalidate() {
        lifecycleLock.lock()
        invalidated = true
        lifecycleLock.unlock()
        super.invalidate()
    }

    /// 显式引用类型并核验 SDK 使用的 Objective-C 名称，避免静态库裁剪。
    public static func register() {
        precondition(NSClassFromString("GycToastModule") == GycToastModule.self)
    }
}
