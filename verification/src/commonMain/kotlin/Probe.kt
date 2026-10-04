package consumer

import io.github.gycrosskit.toast.AppMessageDuration
import io.github.gycrosskit.toast.MessagePlatform

fun show(platform: MessagePlatform) = platform.show("probe", AppMessageDuration.SHORT)
