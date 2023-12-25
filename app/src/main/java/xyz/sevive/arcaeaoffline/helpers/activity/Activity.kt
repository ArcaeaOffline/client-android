package xyz.sevive.arcaeaoffline.helpers.activity

import android.app.Activity
import android.os.Build

fun Activity.getSourcePackageName(): String? {
    return if (intent.`package` != null) {
        intent.`package`
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        referrer?.toString()?.replace("android-app://", "")
    } else {
        null
    }
}
