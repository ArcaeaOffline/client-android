package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
internal fun OcrQueueActions(
    onStartQueue: () -> Unit,
    onStopQueue: () -> Unit,
    onClearAllTasks: () -> Unit,
    queueRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onStartQueue, enabled = !queueRunning) {
            Icon(Icons.Default.PlayArrow, null)
        }

        IconButton(onClick = onStopQueue, enabled = queueRunning) {
            Icon(Icons.Default.Stop, null)
        }

        IconButton(
            onClick = onClearAllTasks,
            enabled = !queueRunning,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ClearAll, null)
        }
    }
}
