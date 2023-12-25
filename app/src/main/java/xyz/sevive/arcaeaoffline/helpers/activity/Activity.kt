package xyz.sevive.arcaeaoffline.helpers.activity

import android.app.Activity
import android.os.Build
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

fun Activity.getSourcePackageName(): String? {
    return if (intent.`package` != null) {
        intent.`package`
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        referrer?.toString()?.replace("android-app://", "")
    } else {
        null
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Activity.calculateWindowSizeClass(): WindowSizeClass {
    return calculateWindowSizeClass(this)
}
