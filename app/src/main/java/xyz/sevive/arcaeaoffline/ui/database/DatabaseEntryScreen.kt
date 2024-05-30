package xyz.sevive.arcaeaoffline.ui.database

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import xyz.sevive.arcaeaoffline.ui.GeneralEntryScreen
import xyz.sevive.arcaeaoffline.ui.database.b30list.DatabaseB30ListScreen
import xyz.sevive.arcaeaoffline.ui.database.manage.DatabaseManageScreen
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DatabaseEntryScreen() {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

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
                DatabaseManageScreen(onNavigateUp = { backPressedDispatcher?.onBackPressed() })
            }

            DatabaseScreenDestinations.AddScore.route -> {
                DatabaseAddPlayResultScreen(onNavigateUp = { backPressedDispatcher?.onBackPressed() })
            }

            DatabaseScreenDestinations.ScoreList.route -> {
                DatabasePlayResultListScreen(onNavigateUp = { backPressedDispatcher?.onBackPressed() })
            }

            DatabaseScreenDestinations.B30.route -> {
                DatabaseB30ListScreen(onNavigateUp = { backPressedDispatcher?.onBackPressed() })
            }
        }
    }
}
