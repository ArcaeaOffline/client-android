package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
internal fun OcrQueueAddTaskActions(
    onPickImages: () -> Unit,
    onPickFolder: () -> Unit,
    enabled: Boolean,
    queueEnqueueCheckerProgress: Pair<Int, Int>?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        Button(
            onClick = { onPickImages() },
            Modifier.weight(1f),
            enabled = enabled,
        ) {
            IconRow {
                Icon(Icons.Default.PhotoLibrary, null)
                Text(stringResource(R.string.ocr_queue_pick_images_button))
            }
        }

        Button(
            onClick = { onPickFolder() },
            Modifier.weight(1f),
            enabled = enabled,
        ) {
            IconRow {
                Icon(Icons.Default.PermMedia, null)
                Text(stringResource(R.string.ocr_queue_pick_folder_button))
            }
        }

        AnimatedVisibility(visible = queueEnqueueCheckerProgress != null) {
            queueEnqueueCheckerProgress?.let {
                CircularProgressIndicator(
                    progress = { it.first.toFloat() / it.second },
                    modifier = Modifier.size(ButtonDefaults.MinHeight - 2.dp)
                )
            } ?: CircularProgressIndicator()
        }
    }
}

@PreviewLightDark
@Composable
private fun OcrQueueAddTaskActionsPreview() {
    var progress by remember { mutableFloatStateOf(0.25f) }
    val progressPair = remember(progress) {
        if (progress == 0f) null
        else Pair((progress * 100).toInt(), 100)
    }

    ArcaeaOfflineTheme {
        Surface {
            Column {
                Slider(value = progress, onValueChange = { progress = it })

                OcrQueueAddTaskActions(
                    onPickImages = {},
                    onPickFolder = {},
                    queueEnqueueCheckerProgress = progressPair,
                    enabled = true,
                )
            }
        }
    }
}
