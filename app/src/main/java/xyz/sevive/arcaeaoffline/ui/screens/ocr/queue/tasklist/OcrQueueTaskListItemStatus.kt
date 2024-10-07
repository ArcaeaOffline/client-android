package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.tasklist

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus


@Composable
internal fun OcrQueueTaskListItemStatus(status: OcrQueueTaskStatus) {
    when (status) {
        OcrQueueTaskStatus.IDLE -> Icon(
            Icons.Default.HistoryToggleOff,
            contentDescription = stringResource(R.string.ocr_queue_status_idle),
        )

        OcrQueueTaskStatus.PROCESSING -> CircularProgressIndicator(
            Modifier.size(Icons.Default.HourglassBottom.defaultHeight),
            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 1.5f,
        )

        OcrQueueTaskStatus.DONE -> Icon(
            Icons.Default.Check,
            contentDescription = stringResource(R.string.ocr_queue_status_done),
            tint = MaterialTheme.colorScheme.secondary,
        )

        OcrQueueTaskStatus.ERROR -> Icon(
            Icons.Default.Error,
            contentDescription = stringResource(R.string.ocr_queue_status_error),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}
