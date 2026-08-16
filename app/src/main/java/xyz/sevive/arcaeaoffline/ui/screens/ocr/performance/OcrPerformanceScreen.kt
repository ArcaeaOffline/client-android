package xyz.sevive.arcaeaoffline.ui.screens.ocr.performance

import android.content.ClipData
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.ocr.device.OcrPerformanceBenchmark
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDate
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedTime
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.LinearProgressIndicatorWrapper
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.preferences.BasePreferencesWidget
import xyz.sevive.arcaeaoffline.ui.components.preferences.SliderPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.navigation.OcrSubScreen

@Composable
fun OcrPerformanceScreen(
    modifier: Modifier = Modifier,
    viewModel: OcrPerformanceScreenViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.onImagesPicked(uris)
            }
        }

    SubScreenContainer(
        title = stringResource(OcrSubScreen.Performance.title),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        LazyColumn(modifier) {
            item {
                ListGroupHeader(stringResource(R.string.ocr_performance_images_title))
            }

            item {
                TextPreferencesWidget(
                    title = stringResource(R.string.ocr_performance_pick_images_button),
                    content =
                        uiState.selectedImageUris.takeIf { it.isNotEmpty() }?.let {
                            pluralStringResource(
                                R.plurals.ocr_performance_picked_images,
                                it.size,
                                it.size,
                            )
                        },
                    leadingSlot = {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    enabled = !uiState.running,
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    trailingSlot =
                        uiState.selectedImageUris.takeIf { it.isNotEmpty() }?.let {
                            {
                                IconButton(
                                    onClick = viewModel::clearImages,
                                    enabled = !uiState.running,
                                    colors =
                                        IconButtonDefaults.iconButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error,
                                        ),
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.ocr_performance_clear_selection),
                                    )
                                }
                            }
                        },
                )
            }

            if (uiState.imageLoadError) {
                item {
                    Text(
                        stringResource(R.string.ocr_performance_image_load_failed),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            item { HorizontalDivider() }

            item {
                ListGroupHeader(stringResource(R.string.ocr_performance_concurrency_title))
            }

            item {
                AnimatedContent(
                    targetState = uiState.running,
                    label = "benchmarkRunningState",
                ) { running ->
                    if (!running) {
                        SliderPreferencesWidget(
                            value = uiState.parallelCount.toFloat(),
                            onValueChange = viewModel::onParallelCountChange,
                            icon = Icons.AutoMirrored.Default.Sort,
                            title = stringResource(R.string.ocr_queue_queue_options_parallel_count),
                            description = uiState.parallelCount.toString(),
                            valueRange = OcrPerformanceScreenViewModel.parallelCountSliderRange,
                            steps = OcrPerformanceScreenViewModel.parallelCountSliderSteps,
                            trailingSlot = {
                                OutlinedButton(
                                    onClick = { viewModel.runBenchmark() },
                                    enabled = uiState.selectedImageUris.isNotEmpty(),
                                ) {
                                    IconRow {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Text(stringResource(R.string.ocr_performance_run_button))
                                    }
                                }
                            },
                        )
                    } else {
                        BasePreferencesWidget(
                            title = {
                                uiState.runningParallel?.let { parallel ->
                                    Text(
                                        stringResource(R.string.ocr_performance_single_progress, parallel),
                                    )
                                }
                            },
                            content = {
                                LinearProgressIndicatorWrapper(
                                    progress = Progress(uiState.progress, uiState.progressTotal),
                                )
                            },
                            trailingSlot = {
                                OutlinedButton(onClick = { viewModel.cancelBenchmark() }) {
                                    IconRow {
                                        Icon(Icons.Default.Stop, contentDescription = null)
                                        Text(stringResource(R.string.ocr_performance_cancel_button))
                                    }
                                }
                            },
                        )
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(
                        stringResource(R.string.ocr_performance_benchmark_error, message),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            uiState.result?.let { result ->
                item { HorizontalDivider() }

                item {
                    ListGroupHeader(stringResource(R.string.ocr_performance_result_title))
                }

                item {
                    ResultCard(
                        parallel = uiState.resultParallel ?: uiState.parallelCount,
                        result = result,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.list_padding)),
                    )
                }
            }

            if (uiState.history.isNotEmpty()) {
                item { HorizontalDivider() }

                item {
                    ListGroupHeader(stringResource(R.string.ocr_performance_history_title))
                }

                // Newest first, easier to compare recent runs
                items(uiState.history.asReversed(), key = { it.uuid }) { entry ->
                    HistoryRow(
                        entry = entry,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    parallel: Int,
    result: OcrPerformanceBenchmark.Result,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val resources = LocalResources.current

    Card(modifier = modifier.then(Modifier.padding(horizontal = 16.dp))) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding)),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.ocr_performance_result_parallel, parallel),
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val text = buildReportText(parallel, result)
                            val clipData = ClipData.newPlainText("OCR Performance", text)
                            clipboard.setClipEntry(clipData.toClipEntry())
                            snackbarHostState.showSnackbar(
                                resources.getString(R.string.ocr_performance_report_copied),
                            )
                        }
                    },
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.ocr_performance_copy_report),
                    )
                }
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.list_padding)))
            KeyValueRow(
                label = stringResource(R.string.ocr_performance_result_median_label),
                value = stringResource(R.string.ocr_performance_result_median_value, result.medianPerImageMs),
                valueStyle = MaterialTheme.typography.titleMedium,
            )
            KeyValueRow(
                label = stringResource(R.string.ocr_performance_result_throughput_label),
                value = stringResource(R.string.ocr_performance_result_throughput_value, result.throughputPerSecond),
                valueStyle = MaterialTheme.typography.titleMedium,
            )
            KeyValueRow(
                label = stringResource(R.string.ocr_performance_result_batches_label),
                value = stringResource(R.string.ocr_performance_result_batches_value, result.batchTimesMs.joinToString("/")),
                valueStyle = MaterialTheme.typography.bodyMedium,
            )
            KeyValueRow(
                label = stringResource(R.string.ocr_performance_result_output_label),
                value =
                    stringResource(
                        if (result.resultsConsistent) {
                            R.string.ocr_performance_result_output_consistent
                        } else {
                            R.string.ocr_performance_result_output_inconsistent
                        },
                    ),
                valueStyle = MaterialTheme.typography.bodyMedium,
                valueColor = if (result.resultsConsistent) Color.Unspecified else MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * Key-value row: label left-aligned (muted), value right-aligned
 */
@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    valueStyle: TextStyle,
    valueColor: Color = Color.Unspecified,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.secondaryItemAlpha(),
        )
        Text(value, style = valueStyle, color = valueColor)
    }
}

/**
 * Generate pure text report for copying
 */
private fun buildReportText(
    parallel: Int,
    result: OcrPerformanceBenchmark.Result,
): String =
    buildString {
        appendLine("OCR Performance (p$parallel)")
        appendLine("median: %.0f ms/image".format(result.medianPerImageMs))
        appendLine("throughput: %.1f it/s".format(result.throughputPerSecond))
        appendLine("batches: ${result.batchTimesMs.joinToString("/")} ms")
        append("consistent: ${result.resultsConsistent}")
    }

@Composable
private fun HistoryRow(
    entry: OcrPerformanceScreenViewModel.HistoryEntry,
    modifier: Modifier = Modifier,
) {
    val timestampText =
        remember(entry.timestamp) {
            entry.timestamp.formatAsLocalizedDate() + "\n" + entry.timestamp.formatAsLocalizedTime()
        }

    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
        verticalAlignment = Alignment.Top,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = dimensionResource(R.dimen.list_padding)),
    ) {
        Text(
            timestampText,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "p%d, %.1f it/s".format(
                    entry.parallel,
                    entry.result.throughputPerSecond,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(
                    R.string.ocr_performance_result_batches_value,
                    entry.result.batchTimesMs.joinToString("/"),
                ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.secondaryItemAlpha(),
            )
            if (!entry.result.resultsConsistent) {
                Text(
                    stringResource(R.string.ocr_performance_result_output_inconsistent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
