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
        when (uiItem.dbItem.status) {
            OcrQueueTaskStatus.DONE -> {
                uiItem.dbItem.playResult?.let { playResult ->
                    ArcaeaPlayResultCard(
                        playResult = playResult,
                        warnings = uiItem.warnings.orEmpty(),
                        chart = uiItem.chart,
                        modifier = modifier,
                    )
                }
            }

            OcrQueueTaskStatus.ERROR -> {
                Text(
                    buildAnnotatedString {
                        withStyle(MaterialTheme.typography.bodySmall.toSpanStyle()) {
                            appendLine(uiItem.dbItem.errorType ?: "No errorType")
                        }
                        append(uiItem.dbItem.errorMessage ?: "No errorMessage")
                    },
                    color = MaterialTheme.colorScheme.error,
                    modifier = modifier,
                )
            }

            else -> {}
        }
    }
}
