package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
internal fun processingColor() = MaterialTheme.colorScheme.secondary

@Composable
internal fun doneColor() = MaterialTheme.colorScheme.primary

@Composable
internal fun errorColor() = MaterialTheme.colorScheme.error

@Composable
private fun rememberItemWidth(parentWidth: Dp, itemCount: Int, totalCount: Int): Dp {
    return remember(parentWidth, itemCount, totalCount) {
        if (totalCount == 0) 0.dp else parentWidth * itemCount / totalCount
    }
}

@Composable
internal fun OcrQueueProgressIndicator(
    taskCounts: OcrQueueScreenViewModel.QueueTaskCounts,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var width by remember { mutableStateOf(0.dp) }
    val doneWidth = rememberItemWidth(width, taskCounts.done, taskCounts.total)
    val errorWidth = rememberItemWidth(width, taskCounts.error, taskCounts.total)
    val processingWidth = rememberItemWidth(width, taskCounts.processing, taskCounts.total)

    Box(
        Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(ProgressIndicatorDefaults.linearTrackColor)
            .onGloballyPositioned {
                width = density.run { it.size.width.toDp() }
            }
            .then(modifier),
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
