package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
internal fun OcrQueueAddTaskActions(
    onPickImages: () -> Unit,
    onPickFolder: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        Button(
            onClick = { onPickImages() },
            Modifier.weight(1f),
            enabled = enabled,
        ) {
            IconRow(icon = { Icon(Icons.Default.PhotoLibrary, null) }) {
                Text(stringResource(R.string.ocr_queue_pick_images_button))
            }
        }

        Button(
            onClick = { onPickFolder() },
            Modifier.weight(1f),
            enabled = enabled,
        ) {
            IconRow(icon = { Icon(Icons.Default.PermMedia, null) }) {
                Text(stringResource(R.string.ocr_queue_pick_folder_button))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OcrQueueAddTaskActionsPreview() {
    ArcaeaOfflineTheme {
        Surface {
            OcrQueueAddTaskActions(
                onPickImages = {},
                onPickFolder = {},
                enabled = true,
            )
        }
    }
}
