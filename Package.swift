// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "GycToastNative",
    platforms: [.iOS(.v15)],
    products: [.library(name: "GycToastNative", targets: ["GycToastNative"])],
    targets: [.target(name: "GycToastNative")]
)
