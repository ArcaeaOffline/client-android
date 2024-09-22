package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


@Composable
internal fun processingColor() = MaterialTheme.colorScheme.secondary

@Composable
internal fun doneColor() = MaterialTheme.colorScheme.primary

@Composable
internal fun errorColor() = MaterialTheme.colorScheme.error

@Composable
private fun rememberItemWidthPercentage(itemCount: Int, totalCount: Int): Float {
    return remember(itemCount, totalCount) {
        if (totalCount == 0) 0f else itemCount.toFloat() / totalCount
    }
}

private fun DrawScope.drawProgressLine(
    offsetPercentage: Float, progressPercentage: Float, color: Color
) {
    val width = size.width
    val height = size.height
    val yOffset = height / 2

    val isLtr = layoutDirection == LayoutDirection.Ltr
    val lineWidth = progressPercentage * width
    val offsetWidth = offsetPercentage * width

    val lineStart = if (isLtr) offsetWidth else width - offsetWidth
    val lineEnd = if (isLtr) offsetWidth + lineWidth else width - (offsetWidth + lineWidth)

    drawLine(
        color = color,
        start = Offset(lineStart, yOffset),
        end = Offset(lineEnd, yOffset),
        strokeWidth = size.height,
    )
}

@Composable
internal fun OcrQueueProgressIndicator(
    taskCounts: OcrQueueScreenViewModel.QueueTaskCounts,
    modifier: Modifier = Modifier,
) {
    val donePercentage = rememberItemWidthPercentage(taskCounts.done, taskCounts.total)
    val errorPercentage = rememberItemWidthPercentage(taskCounts.error, taskCounts.total)
    val processingPercentage = rememberItemWidthPercentage(taskCounts.processing, taskCounts.total)

    val doneColor = doneColor()
    val processingColor = processingColor()
    val errorColor = errorColor()

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(ProgressIndicatorDefaults.linearTrackColor)
            .then(modifier),
    ) {
        drawProgressLine(0f, donePercentage, doneColor)
        drawProgressLine(donePercentage, processingPercentage, processingColor)
        drawProgressLine(donePercentage + processingPercentage, errorPercentage, errorColor)
    }
}
