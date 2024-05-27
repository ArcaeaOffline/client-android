package xyz.sevive.arcaeaoffline.ui.database.manage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButton
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard


@Composable
fun DatabaseManageImport(viewModel: DatabaseManageViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val importPacklistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        fileUri?.let {
            coroutineScope.launch { viewModel.importPacklist(it, context) }
        }
    }

    val importSonglistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        fileUri?.let {
            coroutineScope.launch { viewModel.importSonglist(it, context) }
        }
    }

    val importArcaeaApkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { fileUri ->
        fileUri?.let { uri ->
            context.contentResolver.openInputStream(uri)?.let {
                coroutineScope.launch { viewModel.importArcaeaApkFromInputStream(it, context) }
            }
        }
    }

    val importChartInfoDatabaseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            coroutineScope.launch {
                viewModel.importChartsInfoDatabase(it, context)
            }
        }
    }

    TitleOutlinedCard(title = { padding ->
        IconRow(
            modifier = modifier.padding(padding),
            icon = { Icon(Icons.Default.FileDownload, null) }) {
            Text(stringResource(R.string.database_manage_import_title))
        }
    }, modifier = modifier) { padding ->
        Column(
            Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
        ) {
            TitleOutlinedCard(title = {
                Text(
                    stringResource(R.string.database_manage_import_song_info_title),
                    Modifier.padding(it),
                )
            }) {
                Column(
                    Modifier
                        .padding(it)
                        .fillMaxWidth()
                ) {
                    Button({ importPacklistLauncher.launch("*/*") }) {
                        IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                            Text(stringResource(R.string.database_manage_import_packlist))
                        }
                    }
                    Button({ importSonglistLauncher.launch("*/*") }) {
                        IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                            Text(stringResource(R.string.database_manage_import_songlist))
                        }
                    }

                    Button({ importArcaeaApkLauncher.launch("*/*") }) {
                        IconRow(icon = { Icon(Icons.Default.Android, null) }) {
                            Text(stringResource(R.string.database_manage_import_from_arcaea_apk))
                        }
                    }

                    ArcaeaButton({
                        coroutineScope.launch { viewModel.importArcaeaApkFromInstalled(context) }
                    }) {
                        Text(stringResource(R.string.database_manage_import_from_arcaea_installed))
                    }
                }
            }

            TitleOutlinedCard(title = {
                Text(
                    stringResource(R.string.database_manage_import_chart_info_title),
                    Modifier.padding(it),
                )
            }) {
                Column(
                    Modifier
                        .padding(it)
                        .fillMaxWidth()
                ) {
                    Button(onClick = { importChartInfoDatabaseLauncher.launch("*/*") }) {
                        IconRow(icon = { Icon(Icons.Default.FileOpen, null) }) {
                            Text(stringResource(R.string.database_manage_import_chart_info_database))
                        }
                    }
                }
            }
        }
    }
}
