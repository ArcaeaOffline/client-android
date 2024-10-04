package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
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
    val density = LocalDensity.current
    val iconSizePx = remember(iconRenderSize) { with(density) { iconRenderSize.roundToPx() } }
    val bitmap = remember(iconSizePx) {
        ArcaeaPackageHelper(context).getIcon()?.toBitmap(width = iconSizePx, height = iconSizePx)
    }

    if (forceDisabled) {
        ArcaeaAppIconDisabledContent(modifier)
    } else {
        bitmap?.let {
            Image(bitmap.asImageBitmap(), contentDescription = null, modifier = modifier)
        } ?: ArcaeaAppIconDisabledContent(modifier)
    }
}
