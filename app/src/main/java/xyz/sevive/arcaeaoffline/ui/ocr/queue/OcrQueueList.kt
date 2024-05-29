package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.R


@Composable
fun OcrQueueList(
    ocrQueueViewModel: OcrQueueViewModel,
    onSaveScore: (Int) -> Unit,
) {
    val uiItems by ocrQueueViewModel.uiItems.collectAsStateWithLifecycle()
    val queueRunning by ocrQueueViewModel.queueRunning.collectAsStateWithLifecycle()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
        items(uiItems, key = { it.id }) { uiItem ->
            OcrQueueItem(
                uiItem,
                onDeleteTask = { ocrQueueViewModel.deleteTask(it) },
                deleteEnabled = !queueRunning,
                onEditScore = { taskId, score -> ocrQueueViewModel.modifyTaskScore(taskId, score) },
                onSaveScore = onSaveScore,
            )
        }
    }
}
