package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.database.DatabaseAddScoreScreen
import xyz.sevive.arcaeaoffline.ui.database.DatabaseEntryScreen
import xyz.sevive.arcaeaoffline.ui.database.DatabaseManageScreen
import xyz.sevive.arcaeaoffline.ui.database.DatabaseScoreListScreen

const val DatabaseNavRouteRoot = "database"

enum class DatabaseScreens(val route: String, @StringRes val title: Int) {
    Entry("$DatabaseNavRouteRoot/entry", R.string.develop_placeholder),
    Manage("$DatabaseNavRouteRoot/manage", R.string.database_manage_title),
    AddScore("$DatabaseNavRouteRoot/add_score", R.string.database_add_score_title),
    ScoreList("$DatabaseNavRouteRoot/score_list", R.string.database_score_list_title),
}


fun NavGraphBuilder.databaseGraph(navController: NavController, windowSizeClass: WindowSizeClass) {
    navigation(startDestination = DatabaseScreens.Entry.route, route = DatabaseNavRouteRoot) {
        composable(DatabaseScreens.Entry.route) {
            DatabaseEntryScreen({ navController.navigate(it) }, windowSizeClass)
        }
        composable(DatabaseScreens.Manage.route) {
            DatabaseManageScreen({ navController.navigateUp() })
        }
        composable(DatabaseScreens.AddScore.route) {
            DatabaseAddScoreScreen()
        }
        composable(DatabaseScreens.ScoreList.route) {
            DatabaseScoreListScreen()
        }
    }
}

@Composable
fun DatabaseNavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "$DatabaseNavRouteRoot/empty") {
        composable("$DatabaseNavRouteRoot/empty") {
            Surface(Modifier.fillMaxSize()) { }
        }

        composable(DatabaseScreens.Manage.route) {
            DatabaseManageScreen({ navController.navigateUp() })
        }
        composable(DatabaseScreens.AddScore.route) {
            DatabaseAddScoreScreen()
        }
        composable(DatabaseScreens.ScoreList.route) {
            DatabaseScoreListScreen()
        }
    }
}
