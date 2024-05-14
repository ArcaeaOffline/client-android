package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.helpers.OcrQueueTaskStatus


@Composable
fun OcrQueue(ocrQueueViewModel: OcrQueueViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiItems by ocrQueueViewModel.ocrQueueTasksUiItems.collectAsState()
    val doneTasks = uiItems.filter { it.status == OcrQueueTaskStatus.DONE }
    val scoreValidTasks = uiItems.filter { it.scoreValid }
    val queueRunning by ocrQueueViewModel.queueRunning.collectAsState()

    val onSaveScore = fun(taskId: Int) {
        coroutineScope.launch {
            ocrQueueViewModel.saveTaskScore(taskId, context)
        }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(buildAnnotatedString {
                append(doneTasks.size.toString())
                withStyle(SpanStyle(fontSize = 0.75.em)) {
                    append("/${uiItems.size}")
                }
            })

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { ocrQueueViewModel.startQueue(context) },
                enabled = !queueRunning,
            ) {
                Icon(Icons.Default.PlayArrow, null)
            }
            IconButton(
                onClick = { ocrQueueViewModel.tryStopQueue(context) },
                enabled = queueRunning,
            ) {
                Icon(Icons.Default.Stop, null)
            }
            IconButton(
                onClick = { scoreValidTasks.forEach { onSaveScore(it.id) } },
                enabled = !queueRunning && scoreValidTasks.isNotEmpty()
            ) {
                Icon(Icons.Default.SaveAlt, null)
            }
            IconButton(
                onClick = { ocrQueueViewModel.clearTasks() },
                enabled = !queueRunning,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ClearAll, null)
            }
        }

        OcrQueueList(ocrQueueViewModel, onSaveScore)
    }
}
