package xyz.sevive.arcaeaoffline.ui.screens.database.playdata

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
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
import org.koin.androidx.compose.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.rememberFileChooserLauncher
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ImportLogBottomSheet
import xyz.sevive.arcaeaoffline.ui.components.SettingsGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseSubScreen
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton
import kotlin.time.Clock

private class CreateJsonDocument : ActivityResultContracts.CreateDocument("application/json")

@Composable
fun DatabasePlayDataScreen(
    modifier: Modifier = Modifier,
    viewModel: DatabasePlayDataViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val navContext = LocalListDetailNavigationContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showLogsSheet by rememberSaveable { mutableStateOf(false) }
    if (showLogsSheet) {
        ImportLogBottomSheet(
            onDismissRequest = { showLogsSheet = false },
            logs = uiState.logs,
        )
    }

    val importSt3Launcher = rememberFileChooserLauncher { it?.let { viewModel.importSt3(it, context) } }
    val exportPlayResultsLauncher =
        rememberLauncherForActivityResult(CreateJsonDocument()) { uri ->
            uri?.let { viewModel.exportPlayResults(it, context) }
        }

    SubScreenContainer(
        modifier = modifier,
        title = stringResource(R.string.database_play_data_title),
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
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                SettingsGroupHeader {
                    IconRow {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Text(stringResource(R.string.database_manage_import_title))
                    }
                }
            }

            item {
                TextItem(
                    onClick = { importSt3Launcher.launch("*/*") },
                    title = stringResource(R.string.arcaea_st3),
                    leadingIcon = Icons.Default.FileOpen,
                )
            }

            item {
                SettingsGroupHeader {
                    IconRow {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Text(stringResource(R.string.database_manage_export_title))
                    }
                }
            }

            item {
                TextItem(
                    onClick = {
                        exportPlayResultsLauncher.launch(
                            "arcaea-offline-data-exchange-${Clock.System.now().toEpochMilliseconds()}",
                        )
                    },
                    title = stringResource(R.string.database_manage_export_play_results),
                    leadingIcon = Icons.Default.UploadFile,
                )
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseSubScreen.Deduplicator.title,
                    icon = Icons.Default.CopyAll,
                ) {
                    navContext.navigateToDetail(DatabaseSubScreen.Deduplicator.route)
                }
            }
        }
    }
}
