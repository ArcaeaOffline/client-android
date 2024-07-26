package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import java.text.NumberFormat

@Composable
private fun rememberIsIndeterminate(current: Int, total: Int): Boolean {
    return remember(current, total) { total < 1 || current == -1 }
}

@Composable
private fun rememberPercentage(current: Int, total: Int): Double {
    val isIndeterminate = rememberIsIndeterminate(current, total)
    return remember(current, total) { if (isIndeterminate) 0.0 else current.toDouble() / total }
}

@Composable
private fun rememberProgressLabel(
    current: Int,
    total: Int,
    formatter: NumberFormat
): AnnotatedString {
    val isIndeterminate = rememberIsIndeterminate(current, total)
    val percentage = rememberPercentage(current, total)

    val indeterminateText = remember {
        buildAnnotatedString {
            append("-")
            withStyle(SpanStyle(fontSize = 0.9.em)) { append("/-") }
            append(" (--%)")
        }
    }
    return remember(current, total, formatter) {
        if (isIndeterminate) indeterminateText
        else buildAnnotatedString {
            append(current.toString())
            withStyle(SpanStyle(fontSize = 0.9.em)) { append("/$total") }
            append(" (${formatter.format(percentage)})")
        }
    }
}

object LinearProgressIndicatorWrapperDefaults {
    val formatter: NumberFormat = NumberFormat.getPercentInstance()
    val indeterminateLabel: String? = null
    val determinateLabel: String? = null
}

@Composable
fun LinearProgressIndicatorWrapper(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    indeterminateLabel: String? = LinearProgressIndicatorWrapperDefaults.indeterminateLabel,
    determinateLabel: String? = LinearProgressIndicatorWrapperDefaults.determinateLabel,
    formatter: NumberFormat = LinearProgressIndicatorWrapperDefaults.formatter,
) {
    val isIndeterminate = rememberIsIndeterminate(current, total)
    val percentage = rememberPercentage(current, total)
    val progressLabel = rememberProgressLabel(current, total, formatter)

    val label = remember(isIndeterminate, indeterminateLabel, determinateLabel) {
        if (isIndeterminate) indeterminateLabel else determinateLabel
    }

    Column(modifier.width(IntrinsicSize.Min)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            label?.let { Text(it) }
            Text(progressLabel)
        }

        if (isIndeterminate) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator({ percentage.toFloat() }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun LinearProgressIndicatorWrapper(
    progress: Pair<Int, Int>?,
    modifier: Modifier = Modifier,
    indeterminateLabel: String? = LinearProgressIndicatorWrapperDefaults.indeterminateLabel,
    determinateLabel: String? = LinearProgressIndicatorWrapperDefaults.determinateLabel,
    formatter: NumberFormat = LinearProgressIndicatorWrapperDefaults.formatter,
) {
    LinearProgressIndicatorWrapper(
        current = progress?.first ?: 0,
        total = progress?.second ?: -1,
        modifier = modifier,
        indeterminateLabel = indeterminateLabel,
        determinateLabel = determinateLabel,
        formatter = formatter,
    )
}
