package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.sevive.arcaeaoffline.ui.GeneralEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.database.b30list.DatabaseB30ListScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.manage.DatabaseManageScreen

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DatabaseEntryScreen() {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val onNavigateUp = remember {
        { backPressedDispatcher?.onBackPressed() ?: Unit }
    }

    GeneralEntryScreen(
        navigator = navigator,
        listPane = {
            DatabaseNavEntry(onNavigateToSubRoute = {
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
            })
        },
    ) {
        when (it) {
            DatabaseScreenDestinations.Manage.route -> {
                DatabaseManageScreen(onNavigateUp = onNavigateUp)
            }

            DatabaseScreenDestinations.AddScore.route -> {
                DatabaseAddPlayResultScreen(onNavigateUp = onNavigateUp)
            }

            DatabaseScreenDestinations.ScoreList.route -> {
                DatabasePlayResultListScreen(onNavigateUp = onNavigateUp)
            }

            DatabaseScreenDestinations.B30.route -> {
                DatabaseB30ListScreen(onNavigateUp = onNavigateUp)
            }

            DatabaseScreenDestinations.R30.route -> {
                DatabaseR30ListScreen(onNavigateUp = onNavigateUp)
            }
        }
    }
}
