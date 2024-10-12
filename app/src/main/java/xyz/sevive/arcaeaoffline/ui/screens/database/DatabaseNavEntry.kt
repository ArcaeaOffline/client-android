package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseNavEntry(
    onNavigateToSubRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
    vm: DatabaseNavEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val statusUiState by vm.statusUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreenDestinations.Database.title)) })
        },
        containerColor = Color.Transparent,
    ) {
        LazyColumn(Modifier.padding(it)) {
            item {
                DatabaseStatus(
                    statusUiState,
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.page_padding))
                )
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseScreenDestinations.Manage.title,
                    icon = Icons.Default.Build,
                ) {
                    onNavigateToSubRoute(DatabaseScreenDestinations.Manage.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseScreenDestinations.AddPlayResult.title,
                    icon = Icons.Default.Add,
                ) {
                    onNavigateToSubRoute(DatabaseScreenDestinations.AddPlayResult.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseScreenDestinations.ScoreList.title,
                    icon = Icons.AutoMirrored.Default.List
                ) {
                    onNavigateToSubRoute(DatabaseScreenDestinations.ScoreList.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseScreenDestinations.B30.title,
                    icon = Icons.Default.Star,
                ) {
                    onNavigateToSubRoute(DatabaseScreenDestinations.B30.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseScreenDestinations.R30.title,
                    icon = Icons.Default.History,
                ) {
                    onNavigateToSubRoute(DatabaseScreenDestinations.R30.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseScreenDestinations.Deduplicator.title,
                    icon = Icons.Default.CopyAll,
                ) {
                    onNavigateToSubRoute(DatabaseScreenDestinations.Deduplicator.route)
                }
            }
        }
    }
}
