package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.screens.OcrScreen
import xyz.sevive.arcaeaoffline.ui.screens.OverviewScreen
import xyz.sevive.arcaeaoffline.ui.screens.SettingsScreen


enum class MainScreens(
    val route: String, val icon: @Composable () -> ImageVector, @StringRes val title: Int
) {
    Overview("overview", { Icons.Filled.Dashboard }, R.string.nav_overview),
    Database(DatabaseNavRouteRoot, { Icons.Filled.Storage }, R.string.nav_database),
    Ocr("ocr", { ImageVector.vectorResource(R.drawable.ic_ocr) }, R.string.nav_ocr),
    Settings("settings", { Icons.Filled.Settings }, R.string.nav_settings),
}

@Composable
fun MainNavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = MainScreens.Overview.route) {
        composable(MainScreens.Overview.route) {
            OverviewScreen()
        }

        databaseGraph(navController)

        composable(MainScreens.Ocr.route) {
            OcrScreen()
        }
        composable(MainScreens.Settings.route) {
            SettingsScreen()
        }
    }
}
