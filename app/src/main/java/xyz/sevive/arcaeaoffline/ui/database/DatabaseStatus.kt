package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard


@Composable
fun DatabaseStatusNotInitialized(
    viewModel: DatabaseNavEntryViewModel, modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.database_status_not_initialized), Modifier.weight(1f)
        )
        Button(onClick = { coroutineScope.launch { viewModel.initDatabase() } }) {
            Text(stringResource(R.string.database_status_initialize_button))
        }
    }
}

@Composable
fun DatabaseStatusInitialized(viewModel: DatabaseNavEntryViewModel, modifier: Modifier = Modifier) {
    val packList by viewModel.packList.collectAsStateWithLifecycle()
    val songList by viewModel.songList.collectAsStateWithLifecycle()
    val difficultyList by viewModel.difficultyList.collectAsStateWithLifecycle()
    val chartInfoList by viewModel.chartInfoList.collectAsStateWithLifecycle()
    val scoreList by viewModel.scoreList.collectAsStateWithLifecycle()

    Column(modifier) {
        Text(
            pluralStringResource(
                R.plurals.database_pack_entries, packList.size, packList.size
            )
        )
        Text(
            pluralStringResource(
                R.plurals.database_song_entries, songList.size, songList.size
            )
        )
        Text(
            pluralStringResource(
                R.plurals.database_difficulty_entries, difficultyList.size, difficultyList.size
            )
        )
        Text(
            pluralStringResource(
                R.plurals.database_chart_info_entries, chartInfoList.size, chartInfoList.size
            )
        )
        Text(
            pluralStringResource(
                R.plurals.database_play_result_entries, scoreList.size, scoreList.size
            )
        )
    }
}

@Composable
fun DatabaseStatus(
    modifier: Modifier = Modifier,
    viewModel: DatabaseNavEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val databaseInitialized by viewModel.databaseInitialized.collectAsStateWithLifecycle()
    val databaseVersion by viewModel.databaseVersion.collectAsStateWithLifecycle()

    TitleOutlinedCard(
        title = { padding ->
            Row(
                modifier.padding(padding),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (databaseInitialized) Icons.Default.Info else Icons.Default.Warning, null
                )
                Text(stringResource(R.string.database_status_title))

                Spacer(Modifier.weight(1f))

                Text(
                    stringResource(R.string.database_version_label, databaseVersion ?: 0),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        },
        modifier = modifier,
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (!databaseInitialized) {
                DatabaseStatusNotInitialized(viewModel)
            } else {
                DatabaseStatusInitialized(viewModel)
            }
        }
    }
}
