package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

typealias PercentageFormatter = (Double) -> String

object LinearProgressIndicatorWrapperDefaults {
    val formatter: PercentageFormatter = { percentage ->
        "${(percentage * 100).roundToInt()}%"
    }
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
    formatter: PercentageFormatter = LinearProgressIndicatorWrapperDefaults.formatter,
) {
    val isIndeterminate = total < 1 || current == -1
    val percentage = if (isIndeterminate) 0.0f else (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = percentage,
        label = "ProgressIndicatorAnimation",
    )

    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val progressLabel =
        buildAnnotatedString {
            if (isIndeterminate) {
                append("-")
                withStyle(SpanStyle(color = secondaryColor)) { append("/- (--%)") }
            } else {
                append(current.toString())
                withStyle(SpanStyle(color = secondaryColor)) {
                    append("/$total (${formatter(percentage.toDouble())})")
                }
            }
        }

    val label = if (isIndeterminate) indeterminateLabel else determinateLabel

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            label?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = progressLabel, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isIndeterminate) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun LinearProgressIndicatorWrapper(
    progress: Pair<Int, Int>?,
    modifier: Modifier = Modifier,
    indeterminateLabel: String? = LinearProgressIndicatorWrapperDefaults.indeterminateLabel,
    determinateLabel: String? = LinearProgressIndicatorWrapperDefaults.determinateLabel,
    formatter: PercentageFormatter = LinearProgressIndicatorWrapperDefaults.formatter,
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
