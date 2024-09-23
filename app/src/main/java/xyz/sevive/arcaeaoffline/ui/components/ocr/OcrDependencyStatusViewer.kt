package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyStatus
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.preferences.BasePreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


object OcrDependencyStatusViewerDefaults {
    @Composable
    fun Title(string: String) {
        Text(string)
    }

    @Composable
    fun Summary(string: String) {
        Text(string, Modifier.secondaryItemAlpha(), style = MaterialTheme.typography.bodySmall)
    }

    @Composable
    fun Icon(imageVector: ImageVector) {
        Icon(imageVector, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }

    @Composable
    fun Details(string: String) {
        Text(string)
    }
}

@Composable
fun OcrDependencyStatusViewer(
    title: @Composable () -> Unit,
    summary: (@Composable () -> Unit)?,
    status: OcrDependencyStatus,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    details: (@Composable () -> Unit)? = null,
) {
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            icon = {
                IconRow {
                    icon?.invoke() ?: Icon(Icons.Outlined.Info, contentDescription = null)
                    title()
                }
            },
            title = { OcrDependencyStatusChip(status, expanded = true) },
            text = details,
            confirmButton = { /* Nothing */ },
        )
    }

    BasePreferencesWidget(
        onClick = details?.let { { showDetails = true } },
        title = { title() },
        leadingSlot = icon,
        modifier = modifier,
        content = summary?.let { { it() } },
        trailingSlot = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OcrDependencyStatusChip(status, expanded = false)
                if (details != null) Icon(Icons.Default.MoreHoriz, contentDescription = null)
            }
        },
    )
}

@Composable
fun OcrDependencyStatusViewer(
    title: String,
    status: OcrDependencyStatus,
    summary: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    details: String? = null,
) {
    OcrDependencyStatusViewer(
        title = { OcrDependencyStatusViewerDefaults.Title(title) },
        summary = { summary?.let { OcrDependencyStatusViewerDefaults.Summary(it) } },
        status = status,
        modifier = modifier,
        icon = icon?.let { { OcrDependencyStatusViewerDefaults.Icon(it) } },
        details = details?.let { { OcrDependencyStatusViewerDefaults.Details(it) } },
    )
}

@Composable
fun OcrDependencyStatusViewer(
    title: String,
    status: OcrDependencyStatus,
    summary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    details: String? = null,
) {
    OcrDependencyStatusViewer(
        title = { OcrDependencyStatusViewerDefaults.Title(title) },
        summary = { summary() },
        status = status,
        modifier = modifier,
        icon = icon?.let { { OcrDependencyStatusViewerDefaults.Icon(it) } },
        details = details?.let { { OcrDependencyStatusViewerDefaults.Details(it) } },
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
                title = "Test Dependency 1 w/ Icon",
                icon = Icons.Default.Api,
                summary = "OK Preview",
                status = OcrDependencyStatus.OK,
                details = "and this is ok i think there isn't much details",
            )
            OcrDependencyStatusViewer(
                title = { Text("Test Dep 2") },
                summary = { Text("WARN Preview") },
                status = OcrDependencyStatus.WARNING,
                details = { Text("and this is warn i think there isn't much details") },
            )
            OcrDependencyStatusViewer(
                title = "TDepend 3",
                summary = "ERR Preview",
                status = OcrDependencyStatus.ERROR,
                details = "a".repeat(75) + "ERROR NO" + "O".repeat(283),
            )
        }
    }
}
