package xyz.sevive.arcaeaoffline.ui.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

// find activity from context
// https://stackoverflow.com/a/69235067/16484891
// CC BY-SA 4.0
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
