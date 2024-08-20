package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp


@Composable
internal fun processingColor() = MaterialTheme.colorScheme.secondary

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
                }) {
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
