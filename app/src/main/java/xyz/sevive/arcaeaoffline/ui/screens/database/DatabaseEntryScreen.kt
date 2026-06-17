package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.compose.runtime.Composable
import xyz.sevive.arcaeaoffline.ui.AdaptiveEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult.DatabaseAddPlayResultScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.b30list.DatabaseB30ListScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator.DatabaseDeduplicatorScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.manage.DatabaseManageScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.DatabasePlayResultListScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.r30list.DatabaseR30ListScreen

@Composable
fun DatabaseEntryScreen() =
    AdaptiveEntryScreen(
        listPane = { DatabaseNavEntry() },
        detailPane = { route ->
            when (route) {
                DatabaseSubScreen.Manage.route -> DatabaseManageScreen()
                DatabaseSubScreen.AddPlayResult.route -> DatabaseAddPlayResultScreen()
                DatabaseSubScreen.ScoreList.route -> DatabasePlayResultListScreen()
                DatabaseSubScreen.B30.route -> DatabaseB30ListScreen()
                DatabaseSubScreen.R30.route -> DatabaseR30ListScreen()
                DatabaseSubScreen.Deduplicator.route -> DatabaseDeduplicatorScreen()
            }
        },
    )
