package consumer

import android.content.Context
import io.github.gycrosskit.toast.AndroidMessagePlatform

fun create(context: Context) = AndroidMessagePlatform.get(context)
