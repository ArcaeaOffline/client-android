package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.rememberFileChooserLauncher
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonDefaults
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonState
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget


@Composable
private fun joinImportEntries(entries: List<String>): String {
    return remember(entries) { entries.joinToString(" · ") }
}

@Composable
fun DatabaseManageImport(
    onImportPacklist: (Uri) -> Unit,
    onImportSonglist: (Uri) -> Unit,
    onImportArcaeaApk: (Uri) -> Unit,
    arcaeaButtonState: ArcaeaButtonState,
    onImportFromInstalledArcaea: () -> Unit,
    onImportChartInfoDatabase: (Uri) -> Unit,
    onImportSt3: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val importPacklistLauncher = rememberFileChooserLauncher { it?.let(onImportPacklist) }
    val importSonglistLauncher = rememberFileChooserLauncher { it?.let(onImportSonglist) }
    val importArcaeaApkLauncher = rememberFileChooserLauncher { it?.let(onImportArcaeaApk) }
    val importChartInfoDatabaseLauncher =
        rememberFileChooserLauncher { it?.let(onImportChartInfoDatabase) }
    val importSt3Launcher = rememberFileChooserLauncher { it?.let(onImportSt3) }

    val descPackEntries = stringResource(R.string.database_manage_import_pack_entries)
    val descSongEntries = stringResource(R.string.database_manage_import_song_entries)
    val descDifficultyEntries = stringResource(R.string.database_manage_import_difficulty_entries)
    val descChartInfoEntries = stringResource(R.string.database_manage_import_chart_info_entries)
    val descPlayResultEntries = stringResource(R.string.database_manage_import_play_result_entries)

    Column(modifier) {
        TextPreferencesWidget(
            onClick = { importPacklistLauncher.launch("*/*") },
            title = stringResource(R.string.database_manage_import_packlist),
            content = joinImportEntries(listOf(descPackEntries)),
            leadingIcon = Icons.Default.DataObject,
        )

        TextPreferencesWidget(
            onClick = { importSonglistLauncher.launch("*/*") },
            title = stringResource(R.string.database_manage_import_songlist),
            content = joinImportEntries(listOf(descSongEntries, descDifficultyEntries)),
            leadingIcon = Icons.Default.DataObject,
        )

        TextPreferencesWidget(
            onClick = { importArcaeaApkLauncher.launch("*/*") },
            title = stringResource(R.string.database_manage_import_from_arcaea_apk),
            content = joinImportEntries(
                listOf(descPackEntries, descSongEntries, descDifficultyEntries)
            ),
            leadingIcon = Icons.Default.Android,
        )

        if (arcaeaButtonState == ArcaeaButtonState.NORMAL) {
            TextPreferencesWidget(
                onClick = onImportFromInstalledArcaea,
                title = stringResource(R.string.database_manage_import_from_arcaea_installed),
                content = joinImportEntries(
                    listOf(descPackEntries, descSongEntries, descDifficultyEntries)
                ),
                leadingSlot = { ArcaeaButtonDefaults.Icon(state = arcaeaButtonState) },
            )
        } else {
            TextPreferencesWidget(
                title = stringResource(R.string.arcaea_button_resource_unavailable),
                leadingSlot = { ArcaeaButtonDefaults.Icon(state = arcaeaButtonState) },
                enabled = false,
            )
        }

        TextPreferencesWidget(
            onClick = { importChartInfoDatabaseLauncher.launch("*/*") },
            title = stringResource(R.string.database_manage_import_chart_info_database),
            content = joinImportEntries(listOf(descChartInfoEntries)),
            leadingIcon = ImageVector.vectorResource(R.drawable.ic_database),
        )

        TextPreferencesWidget(
            onClick = { importSt3Launcher.launch("*/*") },
            title = stringResource(R.string.arcaea_st3),
            content = joinImportEntries(listOf(descPlayResultEntries)),
            leadingIcon = Icons.Default.FileOpen,
        )
    }
}
