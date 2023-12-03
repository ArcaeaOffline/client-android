package xyz.sevive.arcaeaoffline.ui.database

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.AnimatedPane
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.calculateListDetailPaneScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreens

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DatabaseEntryScreen() {
    var detailPaneRole by rememberSaveable { mutableStateOf(ListDetailPaneScaffoldRole.List) }
    val state = calculateListDetailPaneScaffoldState(currentPaneDestination = detailPaneRole)
    var selectedScreenRoute: String? by rememberSaveable { mutableStateOf(null) }

    val handleNavigateUp = {
        selectedScreenRoute = null
        detailPaneRole = ListDetailPaneScaffoldRole.List
    }

    BackHandler(detailPaneRole == ListDetailPaneScaffoldRole.Detail) {
        handleNavigateUp()
    }

    ListDetailPaneScaffold(
        scaffoldState = state,
        listPane = {
            AnimatedPane(Modifier) {
                DatabaseNavEntry({
                    selectedScreenRoute = it
                    detailPaneRole = ListDetailPaneScaffoldRole.Detail
                })
            }
        },
    ) {
        AnimatedPane(Modifier.fillMaxSize()) {
            when (selectedScreenRoute) {
                DatabaseScreens.Empty.route -> {}

                DatabaseScreens.Manage.route -> {
                    DatabaseManageScreen(onNavigateUp = handleNavigateUp)
                }

                DatabaseScreens.AddScore.route -> {
                    DatabaseAddScoreScreen(onNavigateUp = handleNavigateUp)
                }

                DatabaseScreens.ScoreList.route -> {
                    DatabaseScoreListScreen(onNavigateUp = handleNavigateUp)
                }

                else -> {
                    Surface(contentColor = MaterialTheme.colorScheme.onSurfaceVariant) {
                        Box {
                            Icon(
                                Icons.AutoMirrored.Default.MenuOpen,
                                null,
                                Modifier
                                    .size(150.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}
