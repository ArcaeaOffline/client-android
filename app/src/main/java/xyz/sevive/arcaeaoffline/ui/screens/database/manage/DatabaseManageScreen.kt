package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader

@Composable
fun DatabaseManageScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatabaseManageViewModel = koinViewModel(),
) {
    val context = LocalContext.current

    val canImportLists by viewModel.canImportLists.collectAsStateWithLifecycle()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showLogsSheet by rememberSaveable { mutableStateOf(false) }
    if (showLogsSheet) {
        ImportLogBottomSheet(
            onDismissRequest = { showLogsSheet = false },
            logs = uiState.logs,
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
                    onImportPacklist = { viewModel.importPacklist(it) },
                    onImportSonglist = { viewModel.importSonglist(it, context) },
                    onImportArcaeaApk = { viewModel.importArcaeaApkFromSelected(it, context) },
                    canImportLists = canImportLists,
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
