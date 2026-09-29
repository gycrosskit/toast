import UIKit

/// A scene-level toast presenter shared by Compose and Kuikly bridges.
@objc public final class GycToastPresenter: NSObject {
    @objc public static let shared = GycToastPresenter()

    private weak var rootController: UIViewController?
    private var overlayWindow: ToastOverlayWindow?
    private weak var bannerView: UIView?
    private var dismissWorkItem: DispatchWorkItem?
    private var generation: UInt64 = 0
    private let requestLock = NSLock()
    private var latestRequest: UInt64 = 0

    private override init() { super.init() }

    /// Bind to the app's stable root controller, never a transient dialog controller.
    @objc public func bind(rootController: UIViewController) {
        if Thread.isMainThread {
            self.rootController = rootController
        } else {
            DispatchQueue.main.async { [weak self, weak rootController] in
                self?.rootController = rootController
            }
        }
    }

    @objc public func show(message: String, longDuration: Bool = false) {
        let text = message.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { return }
        requestLock.lock()
        latestRequest &+= 1
        let request = latestRequest
        requestLock.unlock()
        if Thread.isMainThread {
            showOnMain(text: text, longDuration: longDuration, request: request)
        } else {
            DispatchQueue.main.async { [weak self] in
                self?.showOnMain(text: text, longDuration: longDuration, request: request)
            }
        }
    }

    private func showOnMain(text: String, longDuration: Bool, request: UInt64) {
        requestLock.lock()
        let isLatest = request == latestRequest
        requestLock.unlock()
        guard isLatest else { return }
        dismissWorkItem?.cancel()
        dismissWorkItem = nil
        generation = request
        let currentGeneration = request

        bannerView?.layer.removeAllAnimations()
        bannerView?.removeFromSuperview()
        overlayWindow?.isHidden = true
        overlayWindow = nil

        let scene = rootController?.viewIfLoaded?.window?.windowScene
            ?? UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .first { $0.activationState == .foregroundActive }
        guard let scene else { return }

        // A separate non-key window survives a dialog's window being dismissed.
        let overlay = ToastOverlayWindow(windowScene: scene)
        overlay.windowLevel = .alert + 1
        overlay.backgroundColor = .clear
        let controller = UIViewController()
        controller.view.backgroundColor = .clear
        overlay.rootViewController = controller
        overlay.isHidden = false
        overlayWindow = overlay

        let banner = UIView()
        banner.translatesAutoresizingMaskIntoConstraints = false
        banner.backgroundColor = UIColor.black.withAlphaComponent(0.84)
        banner.layer.cornerRadius = 12
        banner.layer.shadowColor = UIColor.black.cgColor
        banner.layer.shadowOpacity = 0.2
        banner.layer.shadowRadius = 6
        banner.layer.shadowOffset = CGSize(width: 0, height: 2)
        banner.isUserInteractionEnabled = false

        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.text = text
        label.textColor = .white
        label.font = .preferredFont(forTextStyle: .subheadline)
        label.adjustsFontForContentSizeCategory = true
        label.numberOfLines = 0
        label.textAlignment = .center

        banner.addSubview(label)
        controller.view.addSubview(banner)
        NSLayoutConstraint.activate([
            banner.topAnchor.constraint(equalTo: controller.view.safeAreaLayoutGuide.topAnchor, constant: 16),
            banner.centerXAnchor.constraint(equalTo: controller.view.centerXAnchor),
            banner.leadingAnchor.constraint(greaterThanOrEqualTo: controller.view.leadingAnchor, constant: 16),
            banner.trailingAnchor.constraint(lessThanOrEqualTo: controller.view.trailingAnchor, constant: -16),
            banner.widthAnchor.constraint(lessThanOrEqualToConstant: 520),
            label.leadingAnchor.constraint(equalTo: banner.leadingAnchor, constant: 18),
            label.trailingAnchor.constraint(equalTo: banner.trailingAnchor, constant: -18),
            label.topAnchor.constraint(equalTo: banner.topAnchor, constant: 12),
            label.bottomAnchor.constraint(equalTo: banner.bottomAnchor, constant: -12),
        ])
        bannerView = banner
        controller.view.layoutIfNeeded()

        banner.alpha = 0
        banner.transform = CGAffineTransform(translationX: 0, y: -8)
        UIView.animate(withDuration: Self.animationDuration, delay: 0,
                       options: [.beginFromCurrentState, .curveEaseOut, .allowUserInteraction]) {
            banner.alpha = 1
            banner.transform = .identity
        }
        UIAccessibility.post(notification: .announcement, argument: text)

        let dismiss = DispatchWorkItem { [weak self, weak banner] in
            guard let self, let banner,
                  self.generation == currentGeneration,
                  self.bannerView === banner else { return }
            UIView.animate(withDuration: Self.animationDuration, delay: 0,
                           options: [.beginFromCurrentState, .curveEaseIn, .allowUserInteraction]) {
                banner.alpha = 0
                banner.transform = CGAffineTransform(translationX: 0, y: -8)
            } completion: { [weak self, weak banner] _ in
                guard let self, let banner,
                      self.generation == currentGeneration,
                      self.bannerView === banner else { return }
                banner.removeFromSuperview()
                self.overlayWindow?.isHidden = true
                self.overlayWindow = nil
                self.dismissWorkItem = nil
            }
        }
        dismissWorkItem = dismiss
        DispatchQueue.main.asyncAfter(
            deadline: .now() + (longDuration ? Self.longDuration : Self.shortDuration),
            execute: dismiss
        )
    }

    private static let animationDuration = 0.18
    private static let shortDuration = 2.0
    private static let longDuration = 4.0
}

private final class ToastOverlayWindow: UIWindow {
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? { nil }
}
