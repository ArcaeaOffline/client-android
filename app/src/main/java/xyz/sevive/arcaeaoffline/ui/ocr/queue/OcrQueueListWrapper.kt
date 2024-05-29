package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.OcrQueueTaskStatus


@Composable
internal fun IconCountProgressIndicator(
    icon: ImageVector,
    contentDescription: String,
    count: Int,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    CompositionLocalProvider(
        LocalContentColor provides color,
    ) {
        Row {
            Icon(icon, contentDescription, Modifier.scale(0.8f))
            Text(count.toString())
        }
    }
}

@Composable
internal fun processingColor() = MaterialTheme.colorScheme.inversePrimary

@Composable
internal fun doneColor() = MaterialTheme.colorScheme.primary

@Composable
internal fun errorColor() = MaterialTheme.colorScheme.error

@Composable
internal fun OcrQueueProgressIndicator(
    total: Int, processing: Int, done: Int, error: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        VerticalGrid(columns = SimpleGridCells.Fixed(2)) {
            IconCountProgressIndicator(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = stringResource(R.string.ocr_queue_status_idle),
                count = total,
            )
            IconCountProgressIndicator(
                icon = Icons.Default.HourglassTop,
                contentDescription = stringResource(R.string.ocr_queue_status_processing),
                count = processing,
                color = processingColor()
            )
            IconCountProgressIndicator(
                icon = Icons.Default.Check,
                contentDescription = stringResource(R.string.ocr_queue_status_done),
                count = done,
                color = doneColor()
            )
            IconCountProgressIndicator(
                icon = Icons.Default.Close,
                contentDescription = stringResource(R.string.ocr_queue_status_error),
                count = error,
                color = errorColor()
            )
        }

        val density = LocalDensity.current
        var width by remember { mutableStateOf(0.dp) }
        val doneWidth = if (total == 0) 0.dp else width * done / total
        val errorWidth = if (total == 0) 0.dp else width * error / total
        val processingWidth = if (total == 0) 0.dp else width * processing / total

        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(ProgressIndicatorDefaults.linearTrackColor)
                .onGloballyPositioned {
                    width = density.run { it.size.width.toDp() }
                }
        ) {
            Box(
                Modifier
                    .width(doneWidth)
                    .fillMaxHeight()
                    .background(doneColor())
                    .align(Alignment.CenterStart)
            )

            Box(
                Modifier
                    .width(errorWidth)
                    .fillMaxHeight()
                    .offset(x = doneWidth)
                    .background(errorColor())
                    .align(Alignment.CenterStart)
            )

            Box(
                Modifier
                    .width(processingWidth)
                    .fillMaxHeight()
                    .offset(x = doneWidth + errorWidth)
                    .background(processingColor())
                    .align(Alignment.CenterStart)
            )
        }
    }
}

@Composable
fun OcrQueueListWrapper(viewModel: OcrQueueViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiItems by viewModel.uiItems.collectAsStateWithLifecycle()
    val doneTasksCount = uiItems.count { it.status == OcrQueueTaskStatus.DONE }
    val errorTasksCount = uiItems.count { it.status == OcrQueueTaskStatus.ERROR }
    val processingTasksCount = uiItems.count { it.status == OcrQueueTaskStatus.PROCESSING }
    val scoreValidTaskIds = uiItems.filter { it.scoreValidatorWarnings().isEmpty() }.map { it.id }

    val queueRunning by viewModel.queueRunning.collectAsStateWithLifecycle()

    val onSaveScore = fun(taskId: Int) {
        coroutineScope.launch {
            viewModel.saveTaskScore(taskId, context)
        }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OcrQueueProgressIndicator(
                total = uiItems.size,
                processing = processingTasksCount,
                done = doneTasksCount,
                error = errorTasksCount,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = { coroutineScope.launch { viewModel.startQueue(context) } },
                enabled = !queueRunning,
            ) {
                Icon(Icons.Default.PlayArrow, null)
            }
            IconButton(
                onClick = { viewModel.tryStopQueue() },
                enabled = queueRunning,
            ) {
                Icon(Icons.Default.Stop, null)
            }
            IconButton(
                onClick = { scoreValidTaskIds.forEach { onSaveScore(it) } },
                enabled = !queueRunning && scoreValidTaskIds.isNotEmpty()
            ) {
                Icon(Icons.Default.SaveAlt, null)
            }
            IconButton(
                onClick = { viewModel.clearTasks() },
                enabled = !queueRunning,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ClearAll, null)
            }
        }

        OcrQueueList(viewModel, onSaveScore)
    }
}
