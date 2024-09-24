package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
internal fun OcrQueueCategoryItem(
    onClick: () -> Unit,
    icon: ImageVector,
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    tailSlot: @Composable () -> Unit = {},
) {
    val countString = remember(count) { count.toString() }

    TextPreferencesWidget(
        onClick = onClick,
        title = title,
        modifier = modifier,
        leadingSlot = {
            Icon(icon, contentDescription = null, tint = tint)
        },
        trailingSlot = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding)),
            ) {
                Text(countString, Modifier.secondaryItemAlpha())
                tailSlot()
                Icon(Icons.AutoMirrored.Default.ArrowRight, contentDescription = null)
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun OcrQueueCategoryItemPreview() {
    ArcaeaOfflineTheme {
        Surface {
            Column {
                OcrQueueCategoryItem(
                    onClick = { },
                    icon = Icons.Default.MoreHoriz,
                    title = "IDLE",
                    count = 25,
                    tint = MaterialTheme.colorScheme.onSurface
                )

                OcrQueueCategoryItem(
                    onClick = { },
                    icon = Icons.Default.HourglassBottom,
                    title = "PROCESSING",
                    count = 8,
                    tint = MaterialTheme.colorScheme.tertiary
                )

                OcrQueueCategoryItem(
                    onClick = { },
                    icon = Icons.Default.Check,
                    title = "DONE",
                    count = 422,
                    tint = MaterialTheme.colorScheme.primary
                )

                OcrQueueCategoryItem(
                    onClick = { },
                    icon = Icons.Default.Close,
                    title = "ERROR",
                    count = 42,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
