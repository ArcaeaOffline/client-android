package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyStatus
import xyz.sevive.arcaeaoffline.helpers.needsSetup
import xyz.sevive.arcaeaoffline.ui.components.BaseSettingsItem
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusViewer
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusViewer
import xyz.sevive.arcaeaoffline.ui.theme.spacing

private fun OcrFromShareViewModel.OcrDependencyViewersUiState.allDependenciesReady(): Boolean =
    imageHashesDatabase.statusDetail.status() == OcrDependencyStatus.OK &&
        crnnModel.statusDetail.status() == OcrDependencyStatus.OK

@Composable
internal fun OcrFromShareOcrDependencyStatusCard(
    uiState: OcrFromShareViewModel.OcrDependencyViewersUiState?,
    onNavigateToDependencies: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allReady = uiState?.allDependenciesReady() ?: false
    var expanded by rememberSaveable(allReady) { mutableStateOf(!allReady) }

    // Smooths the loading-to-loaded swap and any later content size change.
    Card(modifier.animateContentSize()) {
        OcrFromShareOcrDependencyStatusCardHeader(
            uiState = uiState,
            expanded = expanded,
            onClick = uiState?.let { { expanded = !expanded } },
        )

        uiState?.let {
            AnimatedVisibility(visible = expanded) {
                Column {
                    OcrDependencyImageHashesDatabaseStatusViewer(uiState = it.imageHashesDatabase)
                    OcrDependencyCrnnModelStatusViewer(uiState = it.crnnModel)

                    if (it.imageHashesDatabase.statusDetail.needsSetup()) {
                        TextItem(
                            title = stringResource(R.string.ocr_guide_image_hashes_database_title),
                            content = stringResource(R.string.ocr_guide_image_hashes_database_content),
                            leadingIcon = Icons.Default.CloudDownload,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onNavigateToDependencies,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OcrFromShareOcrDependencyStatusCardHeader(
    uiState: OcrFromShareViewModel.OcrDependencyViewersUiState?,
    expanded: Boolean,
    onClick: (() -> Unit)?,
) {
    val allReady = uiState?.allDependenciesReady() ?: false
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        label = "DependencyCardChevronRotation",
    )

    BaseSettingsItem(
        title = { Text(stringResource(R.string.ocr_dependencies_title)) },
        onClick = onClick,
        leadingSlot = {
            Icon(
                Icons.Default.Api,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingSlot = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState == null) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (allReady) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
                )
            }
        },
    )
}
