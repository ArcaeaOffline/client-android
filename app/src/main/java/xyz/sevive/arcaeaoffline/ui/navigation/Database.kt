package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.database.DatabaseEntryScreen
import xyz.sevive.arcaeaoffline.ui.database.DatabaseManageScreen

const val DatabaseNavRouteRoot = "database"

enum class DatabaseScreens(val route: String, @StringRes val title: Int) {
    Entry("$DatabaseNavRouteRoot/entry", R.string.develop_placeholder),
    Manage("$DatabaseNavRouteRoot/manage", R.string.develop_placeholder)
}


fun NavGraphBuilder.databaseGraph(navController: NavController) {
    navigation(startDestination = DatabaseScreens.Entry.route, route = DatabaseNavRouteRoot) {
        composable(DatabaseScreens.Entry.route) {
            DatabaseEntryScreen({ navController.navigate(it) })
        }
        composable(DatabaseScreens.Manage.route) {
            DatabaseManageScreen({ navController.navigateUp() })
        }
    }
}
