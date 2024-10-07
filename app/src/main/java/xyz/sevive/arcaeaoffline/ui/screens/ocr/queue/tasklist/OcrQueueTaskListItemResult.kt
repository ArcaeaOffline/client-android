package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.tasklist

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreenViewModel


@Composable
internal fun OcrQueueTaskListItemResult(
    uiItem: OcrQueueScreenViewModel.TaskUiItem,
    modifier: Modifier = Modifier,
) {
    Box {
        when (uiItem.status) {
            OcrQueueTaskStatus.DONE -> uiItem.playResult?.let {
                ArcaeaPlayResultCard(
                    playResult = uiItem.playResult,
                    chart = uiItem.chart,
                    modifier = modifier,
                )
            }

            OcrQueueTaskStatus.ERROR -> {
                val exception = uiItem.exception ?: Exception("uiItem exception is null!")

                Text(
                    buildAnnotatedString {
                        withStyle(MaterialTheme.typography.bodySmall.toSpanStyle()) {
                            appendLine(exception::class.qualifiedName)
                        }
                        append(exception.message)
                    },
                    color = MaterialTheme.colorScheme.error,
                    modifier = modifier,
                )
            }

            else -> {}
        }
    }
}
