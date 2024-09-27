package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.context.findActivity
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDate
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedTime
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun LogsBottomSheet(
    onDismissRequest: () -> Unit,
    logObjects: List<DatabaseManageViewModel.LogObject>,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val widthSizeClass =
        context.findActivity()?.let { calculateWindowSizeClass(it) }?.widthSizeClass
            ?: WindowWidthSizeClass.Compact

    val lazyColumnState = rememberLazyListState()
    val showScrollToTopButton by remember {
        derivedStateOf { lazyColumnState.firstVisibleItemIndex > 0 }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showScrollToTopButton,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch { lazyColumnState.animateScrollToItem(0) }
                        },
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    }
                }
            },
        ) { contentPadding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.page_padding))
                    .padding(contentPadding),
                state = lazyColumnState,
            ) {
                if (logObjects.isEmpty()) {
                    item {
                        EmptyScreen(Modifier.fillMaxSize())
                    }
                }

                items(logObjects, key = { it.uuid }) {
                    val timestampText = remember(widthSizeClass) {
                        if (widthSizeClass >= WindowWidthSizeClass.Medium) {
                            it.timestamp.formatAsLocalizedDateTime()
                        } else {
                            it.timestamp.formatAsLocalizedDate() + "\n" + it.timestamp.formatAsLocalizedTime()
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(R.dimen.list_padding))
                            .animateItem(),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            Modifier.secondaryItemAlpha(),
                            horizontalAlignment = Alignment.End,
                        ) {
                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Normal
                                )
                            ) {
                                Text(timestampText, textAlign = TextAlign.End)
                                it.tag?.let { tag -> Text("[${tag}]") }
                            }
                        }

                        Text(it.message, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseManageScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatabaseManageViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current

    val importArcaeaApkFromInstalledButtonState by viewModel.importArcaeaApkFromInstalledButtonState.collectAsStateWithLifecycle()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showLogsSheet by rememberSaveable { mutableStateOf(false) }
    if (showLogsSheet) {
        LogsBottomSheet(
            onDismissRequest = { showLogsSheet = false },
            logObjects = uiState.logObjects,
        )
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(R.string.database_manage_title),
        actions = {
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = { showLogsSheet = true }) {
                    Icon(Icons.Default.PendingActions, contentDescription = null)
                }

                if (uiState.isWorking) {
                    CircularProgressIndicator()
                }
            }
        },
    ) {
        LazyColumn(modifier) {
            item {
                ListGroupHeader {
                    IconRow {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Text(stringResource(R.string.database_manage_import_title))
                    }
                }
            }

            item {
                DatabaseManageImport(
                    onImportPacklist = { viewModel.importPacklist(it, context) },
                    onImportSonglist = { viewModel.importSonglist(it, context) },
                    onImportArcaeaApk = { viewModel.importArcaeaApkFromSelected(it, context) },
                    arcaeaButtonState = importArcaeaApkFromInstalledButtonState,
                    onImportFromInstalledArcaea = { viewModel.importArcaeaApkFromInstalled(context) },
                    onImportChartInfoDatabase = { viewModel.importChartsInfoDatabase(it, context) },
                    onImportSt3 = { viewModel.importSt3(it, context) },
                    Modifier.fillMaxWidth(),
                )
            }

            item { HorizontalDivider() }

            item {
                ListGroupHeader {
                    IconRow {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Text(stringResource(R.string.database_manage_export_title))
                    }
                }
            }

            item {
                DatabaseManageExport(
                    onExportPlayResults = { viewModel.exportPlayResults(it, context) },
                    Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
