package xyz.sevive.arcaeaoffline.ui.navigation

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


sealed class RootNavItem(
    var title: Int, var icon: @Composable () -> ImageVector, var route: String
) {
    object Overview : RootNavItem(R.string.nav_overview, { Icons.Filled.Dashboard }, "overview")
    object Database : RootNavItem(R.string.nav_database, { Icons.Filled.Storage }, "database")
    object Ocr :
        RootNavItem(R.string.nav_ocr, { ImageVector.vectorResource(R.drawable.ic_ocr) }, "ocr")

    object Settings : RootNavItem(R.string.nav_settings, { Icons.Filled.Settings }, "settings")
}

@Composable
fun MainNavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = RootNavItem.Overview.route) {
        composable(RootNavItem.Overview.route) {
            OverviewScreen()
        }

        databaseGraph(navController)

        composable(RootNavItem.Ocr.route) {
            OcrScreen()
        }
        composable(RootNavItem.Settings.route) {
            SettingsScreen()
        }
    }
}
