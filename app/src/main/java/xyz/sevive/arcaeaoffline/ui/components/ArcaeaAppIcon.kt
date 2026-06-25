package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.fetch.newAppIconUri
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper

@Composable
private fun ArcaeaAppIconDisabledContent(modifier: Modifier) {
    Icon(Icons.Default.Cancel, contentDescription = null, modifier = modifier)
}

@Composable
fun ArcaeaAppIcon(
    modifier: Modifier = Modifier,
    forceDisabled: Boolean = false,
    iconRenderSize: Dp = 24.dp,
) {
    val context = LocalContext.current
    val packageInfo = remember { ArcaeaPackageHelper(context).getPackageInfo() }
    val appIconUri =
        packageInfo?.let {
            @Suppress("DEPRECATION")
            newAppIconUri(packageName = it.packageName, versionCode = it.versionCode)
        }

    if (forceDisabled || appIconUri == null) {
        ArcaeaAppIconDisabledContent(modifier.size(iconRenderSize))
    } else {
        AsyncImage(appIconUri, contentDescription = null, modifier.size(iconRenderSize))
    }
}
