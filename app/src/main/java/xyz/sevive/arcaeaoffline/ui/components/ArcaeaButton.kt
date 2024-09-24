package xyz.sevive.arcaeaoffline.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


enum class ArcaeaButtonState {
    NORMAL, NOT_INSTALLED, RESOURCE_UNAVAILABLE
}

internal fun defaultState(packageHelper: ArcaeaPackageHelper): ArcaeaButtonState {
    return if (packageHelper.isInstalled()) {
        ArcaeaButtonState.NORMAL
    } else {
        ArcaeaButtonState.NOT_INSTALLED
    }
}

@Composable
internal fun arcaeaIconBitmapPainter(packageHelper: ArcaeaPackageHelper): BitmapPainter? {
    val iconDrawable = packageHelper.getIcon() ?: return null
    val iconPx = LocalDensity.current.run { 24.dp.toPx() }.toInt()
    val iconBitmap = iconDrawable.toBitmap(iconPx, iconPx).asImageBitmap()
    val iconBitmapPainter = remember(iconBitmap) { BitmapPainter(iconBitmap) }
    return iconBitmapPainter
}

object ArcaeaButtonDefaults {
    fun state(packageHelper: ArcaeaPackageHelper): ArcaeaButtonState {
        return defaultState(packageHelper)
    }

    fun state(context: Context): ArcaeaButtonState {
        return state(ArcaeaPackageHelper(context))
    }

    @Composable
    fun state(packageHelper: ArcaeaPackageHelper? = null): ArcaeaButtonState {
        return state(packageHelper ?: ArcaeaPackageHelper(LocalContext.current))
    }

    fun enabled(state: ArcaeaButtonState): Boolean {
        return when (state) {
            ArcaeaButtonState.NORMAL, ArcaeaButtonState.RESOURCE_UNAVAILABLE -> true
            else -> false
        }
    }

    @Composable
    fun title(state: ArcaeaButtonState, defaultTitle: String): String {
        return when (state) {
            ArcaeaButtonState.NORMAL -> defaultTitle
            ArcaeaButtonState.NOT_INSTALLED -> stringResource(R.string.general_arcaea_not_installed)
            ArcaeaButtonState.RESOURCE_UNAVAILABLE -> stringResource(R.string.arcaea_button_resource_unavailable)
        }
    }

    @Composable
    fun ArcaeaIcon(enabled: Boolean) {
        val context = LocalContext.current
        val packageHelper = remember { ArcaeaPackageHelper(context) }
        val arcaeaIcon = arcaeaIconBitmapPainter(packageHelper)

        when {
            enabled && arcaeaIcon != null -> Image(arcaeaIcon, contentDescription = null)
            enabled -> Icon(Icons.Default.Settings, contentDescription = null)
            else -> Icon(Icons.Default.Cancel, contentDescription = null)
        }
    }
}

@Composable
fun ArcaeaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ArcaeaButtonState = ArcaeaButtonDefaults.state(),
    enabled: Boolean = ArcaeaButtonDefaults.enabled(state),
    enabledContent: @Composable () -> Unit,
) {
    val colors = if (state != ArcaeaButtonState.RESOURCE_UNAVAILABLE) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.buttonColors(
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        )
    }

    Button(onClick = onClick, modifier, enabled = enabled, colors = colors) {
        IconRow {
            ArcaeaButtonDefaults.ArcaeaIcon(enabled = enabled)

            when (state) {
                ArcaeaButtonState.NORMAL -> enabledContent()

                ArcaeaButtonState.NOT_INSTALLED -> {
                    Text(stringResource(R.string.general_arcaea_not_installed))
                }

                ArcaeaButtonState.RESOURCE_UNAVAILABLE -> {
                    Text(stringResource(R.string.arcaea_button_resource_unavailable))
                }
            }
        }
    }
}

@Preview
@Composable
private fun ArcaeaButtonPreview() {
    ArcaeaOfflineTheme {
        Surface {
            Column {
                ArcaeaButton(onClick = {}, state = ArcaeaButtonState.NOT_INSTALLED) {
                    Text(ArcaeaButtonState.NOT_INSTALLED.toString())
                }

                ArcaeaButton(onClick = {}, state = ArcaeaButtonState.RESOURCE_UNAVAILABLE) {
                    Text(ArcaeaButtonState.RESOURCE_UNAVAILABLE.toString())
                }
            }
        }
    }
}
