package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.dimensionResource
import xyz.sevive.arcaeaoffline.R


@Composable
fun OcrQueueList(
    ocrQueueViewModel: OcrQueueViewModel,
    onSaveScore: (OcrQueueTask) -> Unit,
) {
    val tasks by ocrQueueViewModel.ocrQueueTasks.collectAsState()
    val queueRunning by ocrQueueViewModel.queueRunning.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
        items(tasks, key = { it.id }) { task ->
            OcrQueueItem(
                task,
                onDeleteTask = { ocrQueueViewModel.deleteTask(it.id) },
                deleteEnabled = !queueRunning,
                onEditScore = { ocrQueueViewModel.editScore(task.id, it) },
                onSaveScore = onSaveScore,
            )
        }
    }
}
