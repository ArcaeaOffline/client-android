package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.helpers.ArcaeaPackageHelper

@Composable
fun ArcaeaButtonWrapper(
    iconSize: Dp? = null,
    content: @Composable (iconPainter: Painter, arcaeaInstalled: Boolean) -> Unit,
) {
    val context = LocalContext.current

    val iconPx = LocalDensity.current.run {
        (iconSize ?: Icons.Default.Cancel.defaultHeight).toPx()
    }.toInt()

    val arcaeaPackageHelper = ArcaeaPackageHelper(context)

    val arcaeaIcon = if (arcaeaPackageHelper.isInstalled()) {
        arcaeaPackageHelper.getIcon()
    } else null
    val arcaeaIconBitmap = arcaeaIcon?.toBitmap(iconPx, iconPx)?.asImageBitmap()
    val arcaeaIconBitmapPainter = remember(arcaeaIconBitmap) {
        if (arcaeaIconBitmap != null) BitmapPainter(arcaeaIconBitmap) else null
    }

    val iconPainter: Painter =
        arcaeaIconBitmapPainter ?: rememberVectorPainter(Icons.Default.Cancel)

    content(iconPainter, arcaeaPackageHelper.isInstalled())
}


@Composable
fun ArcaeaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp? = null,
    iconModifier: Modifier = Modifier,
    enabledContent: @Composable () -> Unit,
) {
    ArcaeaButtonWrapper(iconSize) { iconPainter, arcaeaInstalled ->
        Button(onClick = onClick, modifier, enabled = arcaeaInstalled) {
            IconRow(icon = {
                if (arcaeaInstalled) {
                    Image(iconPainter, null, iconModifier)
                } else {
                    Icon(Icons.Default.Cancel, null, iconModifier)
                }
            }) {
                if (arcaeaInstalled) enabledContent() else {
                    Text(stringResource(R.string.general_arcaea_not_installed))
                }
            }
        }
    }
}
