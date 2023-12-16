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
import androidx.compose.material3.LocalContentColor
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.models.DatabaseCommonFunctionsViewModel


@Composable
fun OcrQueue(
    ocrQueueViewModel: OcrQueueViewModel,
    databaseCommonFunctionsViewModel: DatabaseCommonFunctionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val tasks by ocrQueueViewModel.ocrQueueTasks.collectAsState()
    val doneTasks = tasks.filter { it.status == OcrQueueStatus.DONE }
    val scoreValidTasks = tasks.filter { it.scoreValid }
    val queueRunning by ocrQueueViewModel.queueRunning.collectAsState()

    val onSaveScore = fun(task: OcrQueueTask) {
        if (task.score == null) return

        coroutineScope.launch {
            databaseCommonFunctionsViewModel.upsertScore(task.score!!)
            ocrQueueViewModel.deleteTask(task.id)
        }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(buildAnnotatedString {
                append(doneTasks.size.toString())
                withStyle(SpanStyle(fontSize = 0.75.em)) {
                    append("/${tasks.size}")
                }
            })

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { coroutineScope.launch { ocrQueueViewModel.startQueue(context) } },
                enabled = !queueRunning,
            ) {
                Icon(Icons.Default.PlayArrow, null)
            }
            IconButton(onClick = { ocrQueueViewModel.tryStopQueue() }, enabled = queueRunning) {
                Icon(Icons.Default.Stop, null)
            }
            IconButton(
                onClick = { scoreValidTasks.forEach { onSaveScore(it) } },
                enabled = !queueRunning && scoreValidTasks.isNotEmpty()
            ) {
                Icon(Icons.Default.SaveAlt, null)
            }
            IconButton(
                onClick = { tasks.map { it.id }.forEach { ocrQueueViewModel.deleteTask(it) } },
                enabled = !queueRunning,
            ) {
                Icon(
                    Icons.Default.ClearAll,
                    null,
                    tint = if (!queueRunning) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            }
        }

        OcrQueueList(ocrQueueViewModel, onSaveScore)
    }
}
