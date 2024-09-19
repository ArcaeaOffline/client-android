package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseNavEntry(
    onNavigateToSubRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreenDestinations.Database.title)) })
        },
        containerColor = Color.Transparent,
    ) {
        LazyColumn(Modifier.padding(it)) {
            item {
                DatabaseStatus(Modifier.fillMaxWidth())
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
                    titleResId = DatabaseScreenDestinations.AddScore.title,
                    icon = Icons.Default.Add,
                ) {
                    onNavigateToSubRoute(DatabaseScreenDestinations.AddScore.route)
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
        }
    }
}
