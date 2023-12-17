package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material.icons.twotone.QuestionMark
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

enum class OcrDependencyStatus {
    OK, ERROR, WARN, UNKNOWN
}

@Composable
fun OcrDependencyItemStatus(
    modifier: Modifier = Modifier,
    title: @Composable ColumnScope.() -> Unit,
    label: @Composable ColumnScope.() -> Unit,
    status: OcrDependencyStatus,
    details: (@Composable () -> Unit)? = {},
) {
    var showDetails by remember { mutableStateOf(false) }
    val expandArrowRotateDegree: Float by animateFloatAsState(
        if (showDetails) 180f else 0f, label = "expandArrowRotate"
    )

    Surface(
        modifier.clickable { showDetails = !showDetails },
        color = Color.Transparent,
    ) {
        Column(modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier.weight(1f)) {
                    title()
                    label()
                }

                var statusIcon = Icons.TwoTone.QuestionMark
                var statusText = "UNKNOWN"
                var statusColor = MaterialTheme.colorScheme.secondary

                when (status) {
                    OcrDependencyStatus.OK -> {
                        statusIcon = Icons.TwoTone.CheckCircle
                        statusText = "OK"
                        statusColor = MaterialTheme.colorScheme.primary
                    }

                    OcrDependencyStatus.WARN -> {
                        statusIcon = Icons.TwoTone.Warning
                        statusText = "WARN"
                        statusColor = MaterialTheme.colorScheme.tertiary
                    }

                    OcrDependencyStatus.ERROR -> {
                        statusIcon = Icons.TwoTone.Error
                        statusText = "ERROR"
                        statusColor = MaterialTheme.colorScheme.error
                    }

                    else -> {}
                }

                if (details != null) {
                    TextButton(onClick = { showDetails = !showDetails }) {
                        Icon(
                            Icons.Default.ExpandMore, "", modifier.rotate(expandArrowRotateDegree)
                        )
                    }
                }

                Row(modifier, verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        statusIcon,
                        "OCR dependency status icon",
                        modifier.padding(end = 2.dp),
                        statusColor
                    )
                    Text(
                        statusText, color = statusColor, style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (details != null) {
                AnimatedVisibility(visible = showDetails) { details() }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OcrDependencyItemStatusPreview(modifier: Modifier = Modifier) {
    ArcaeaOfflineTheme {
        Card(modifier.padding(8.dp)) {
            Text(
                "Test Dependencies",
                modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )

            OcrDependencyItemStatus(
                title = { Text("Test Dependency 1 No Details") },
                label = { Text("Unknown Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.UNKNOWN,
                modifier = modifier
            )
            OcrDependencyItemStatus(
                title = { Text("Test Dependency 1") },
                label = { Text("OK Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.OK,
                details = {
                    Text(
                        "and this is ok i think there isn't much details",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = modifier
            )
            OcrDependencyItemStatus(
                title = { Text("Test Dep 2") },
                label = { Text("WARN Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.WARN,
                details = {
                    Text(
                        "and this is warn i think there isn't much details",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = modifier
            )
            OcrDependencyItemStatus(
                title = { Text("TDepend 3") },
                label = { Text("ERR Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.ERROR,
                details = {
                    Text(
                        "a".repeat(75) + "ERROR NO" + "O".repeat(283),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = modifier
            )
        }
    }
}
