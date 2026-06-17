package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseSubScreen
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreen
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseNavEntry(
    modifier: Modifier = Modifier,
    vm: DatabaseNavEntryViewModel = koinViewModel(),
) {
    val navContext = LocalListDetailNavigationContext.current
    val statusUiState by vm.statusUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreen.Database.title)) })
        },
        containerColor = Color.Transparent,
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(it),
            contentPadding = it,
        ) {
            item {
                DatabaseStatus(
                    statusUiState,
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.page_padding)),
                )
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseSubScreen.Manage.title,
                    icon = Icons.Default.Build,
                ) {
                    navContext.navigateToDetail(DatabaseSubScreen.Manage.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseSubScreen.AddPlayResult.title,
                    icon = Icons.Default.Add,
                ) {
                    navContext.navigateToDetail(DatabaseSubScreen.AddPlayResult.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseSubScreen.ScoreList.title,
                    icon = Icons.AutoMirrored.Default.List,
                ) {
                    navContext.navigateToDetail(DatabaseSubScreen.ScoreList.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseSubScreen.B30.title,
                    icon = Icons.Default.Star,
                ) {
                    navContext.navigateToDetail(DatabaseSubScreen.B30.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = DatabaseSubScreen.R30.title,
                    icon = Icons.Default.History,
                ) {
                    navContext.navigateToDetail(DatabaseSubScreen.R30.route)
                }
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
