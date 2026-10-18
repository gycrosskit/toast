package consumer

import io.github.gycrosskit.toast.IosMessageBridge
import io.github.gycrosskit.toast.IosMessagePlatform

fun create(bridge: IosMessageBridge) = IosMessagePlatform(bridge)
