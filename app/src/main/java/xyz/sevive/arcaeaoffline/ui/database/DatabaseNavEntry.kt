package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.components.ActionCard
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreens

@Composable
fun DatabaseStatusNotInitialized(
    viewModel: DatabaseNavEntryViewModel,
    modifier: Modifier = Modifier
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
    val packList by viewModel.packList.collectAsState()
    val songList by viewModel.songList.collectAsState()
    val difficultyList by viewModel.difficultyList.collectAsState()
    val chartInfoList by viewModel.chartInfoList.collectAsState()
    val scoreList by viewModel.scoreList.collectAsState()

    Column(modifier) {
        Text(stringResource(R.string.database_status_intro))

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
                R.plurals.database_score_entries, scoreList.size, scoreList.size
            )
        )
    }
}

@Composable
fun DatabaseStatusComponent(
    modifier: Modifier = Modifier,
    viewModel: DatabaseNavEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val propertyVersion by viewModel.propertyVersion.collectAsState()

    val databaseInitialized = propertyVersion != null

    TitleOutlinedCard(
        title = { padding ->
            Row(
                modifier.padding(padding),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_icon_text_padding)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (databaseInitialized) Icons.Default.Info else Icons.Default.Warning, null
                )
                Text(stringResource(R.string.database_status_title))
            }
        },
        modifier = modifier,
        titleContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
        titleContentColor = MaterialTheme.colorScheme.primary,
    ) { padding ->
        if (!databaseInitialized) {
            DatabaseStatusNotInitialized(viewModel, Modifier.padding(padding))
        } else {
            DatabaseStatusInitialized(viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
fun DatabaseNavEntry(
    onNavigateToSubRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            Text(
                stringResource(R.string.nav_database),
                Modifier.padding(0.dp, dimensionResource(R.dimen.general_page_padding)),
                style = MaterialTheme.typography.titleLarge,
            )
        },
    ) {
        LazyColumn(
            Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.main_screen_list_arrangement_spaced_by)),
        ) {
            item {
                DatabaseStatusComponent(Modifier.fillMaxWidth())
            }

            item {
                ActionCard(
                    onClick = { onNavigateToSubRoute(DatabaseScreens.Manage.route) },
                    title = stringResource(DatabaseScreens.Manage.title),
                    headSlot = {
                        Icon(Icons.Default.Build, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }

            item {
                ActionCard(
                    onClick = { onNavigateToSubRoute(DatabaseScreens.AddScore.route) },
                    title = stringResource(DatabaseScreens.AddScore.title),
                    headSlot = {
                        Icon(Icons.Default.Add, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }

            item {
                ActionCard(
                    onClick = { onNavigateToSubRoute(DatabaseScreens.ScoreList.route) },
                    title = stringResource(DatabaseScreens.ScoreList.title),
                    headSlot = {
                        Icon(Icons.AutoMirrored.Default.List, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }
        }
    }
}
