package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.database.DatabaseEntryScreen
import xyz.sevive.arcaeaoffline.ui.ocr.OcrEntryScreen
import xyz.sevive.arcaeaoffline.ui.overview.OverviewScreen
import xyz.sevive.arcaeaoffline.ui.settings.SettingsScreen


enum class MainScreenDestinations(
    val route: String, val icon: @Composable () -> ImageVector, @StringRes val title: Int
) {
    Overview(
        "overview",
        { Icons.Filled.Dashboard },
        R.string.nav_overview,
    ),
    Database(
        DatabaseNavRouteRoot,
        { ImageVector.vectorResource(R.drawable.ic_database) },
        R.string.nav_database,
    ),
    Ocr(
        OcrNavRouteRoot,
        { ImageVector.vectorResource(R.drawable.ic_ocr) },
        R.string.nav_ocr,
    ),
    Settings(
        "settings",
        { Icons.Filled.Settings },
        R.string.nav_settings,
    ),
}


@Composable
fun MainNavigationGraph(mainNavController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        mainNavController,
        startDestination = MainScreenDestinations.Overview.route,
        modifier = modifier
    ) {
        composable(MainScreenDestinations.Overview.route) {
            OverviewScreen()
        }

        composable(MainScreenDestinations.Database.route) {
            DatabaseEntryScreen()
        }

        composable(MainScreenDestinations.Ocr.route) {
            OcrEntryScreen()
        }

        composable(MainScreenDestinations.Settings.route) {
            SettingsScreen()
        }
    }
}
