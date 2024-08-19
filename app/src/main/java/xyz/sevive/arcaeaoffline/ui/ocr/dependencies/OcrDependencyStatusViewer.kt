package xyz.sevive.arcaeaoffline.ui.ocr.dependencies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyStatus
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
internal fun OcrDependencyStatusViewer(
    title: @Composable ColumnScope.() -> Unit,
    summary: (@Composable ColumnScope.() -> Unit)?,
    status: OcrDependencyStatus,
    modifier: Modifier = Modifier,
    details: (@Composable () -> Unit)? = null,
) {
    var showDetails by remember { mutableStateOf(false) }

    val statusChipExpanded = remember(showDetails) {
        if (details == null) true else showDetails
    }
    val expandArrowRotateDegree: Float by animateFloatAsState(
        if (showDetails) 180f else 0f, label = "expandArrowRotate"
    )

    Column(
        Modifier
            .clickable { showDetails = !showDetails }
            .padding(8.dp)
            .then(modifier)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier.weight(1f)) {
                title()
                summary?.let { it() }
            }

            OcrDependencyStatusChip(status, expanded = statusChipExpanded)

            details?.let {
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    Modifier.graphicsLayer { rotationZ = expandArrowRotateDegree },
                )
            }
        }

        details?.let {
            AnimatedVisibility(visible = showDetails) { it() }
        }
    }
}

@Composable
internal fun OcrDependencyStatusViewer(
    title: String,
    status: OcrDependencyStatus,
    summary: String?,
    modifier: Modifier = Modifier,
    details: String? = null,
) {
    OcrDependencyStatusViewer(
        title = { Text(title) },
        summary = { summary?.let { Text(it, style = MaterialTheme.typography.labelMedium) } },
        status = status,
        modifier = modifier,
        details = details?.let { { Text(it, style = MaterialTheme.typography.labelMedium) } },
    )
}

@Composable
internal fun OcrDependencyStatusViewer(
    title: String,
    status: OcrDependencyStatus,
    summary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    details: String? = null,
) {
    OcrDependencyStatusViewer(
        title = { Text(title) },
        summary = { summary() },
        status = status,
        modifier = modifier,
        details = details?.let { { Text(it, style = MaterialTheme.typography.labelMedium) } },
    )
}

@PreviewLightDark
@Composable
private fun OcrDependencyStatusViewerPreview() {
    ArcaeaOfflineTheme {
        Card(Modifier.padding(8.dp)) {
            Text(
                "Test Dependencies",
                Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )

            OcrDependencyStatusViewer(
                title = "Test Dependency 1 No Details",
                summary = "Unknown Preview",
                status = OcrDependencyStatus.UNKNOWN,
            )
            OcrDependencyStatusViewer(
                title = "Test Dependency 1",
                summary = "OK Preview",
                status = OcrDependencyStatus.OK,
                details = "and this is ok i think there isn't much details",
            )
            OcrDependencyStatusViewer(
                title = { Text("Test Dep 2") },
                summary = { Text("WARN Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.WARNING,
                details = {
                    Text(
                        "and this is warn i think there isn't much details",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
            )
            OcrDependencyStatusViewer(
                title = { Text("TDepend 3") },
                summary = { Text("ERR Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.ERROR,
                details = {
                    Text(
                        "a".repeat(75) + "ERROR NO" + "O".repeat(283),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
            )
        }
    }
}
