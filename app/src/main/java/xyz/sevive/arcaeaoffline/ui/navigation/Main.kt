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
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabaseEntryScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.OcrEntryScreen
import xyz.sevive.arcaeaoffline.ui.screens.overview.OverviewScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.SettingsScreen

const val OVERVIEW_NAV_ROUTE_ROOT = "overview"

enum class MainScreen(
    val route: String,
    val icon: @Composable () -> ImageVector,
    @StringRes val title: Int,
) {
    Overview(
        OVERVIEW_NAV_ROUTE_ROOT,
        { Icons.Filled.Dashboard },
        R.string.nav_overview,
    ),
    Database(
        DATABASE_NAV_ROUTE_ROOT,
        { ImageVector.vectorResource(R.drawable.ic_database) },
        R.string.nav_database,
    ),
    Ocr(
        OCR_NAV_ROUTE_ROOT,
        { ImageVector.vectorResource(R.drawable.ic_ocr) },
        R.string.nav_ocr,
    ),
    Settings(
        SETTINGS_NAV_ROUTE_ROOT,
        { Icons.Filled.Settings },
        R.string.nav_settings,
    ),
}

@Composable
fun MainNavigationGraph(
    mainNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        mainNavController,
        startDestination = MainScreen.Overview.route,
        modifier = modifier,
    ) {
        composable(MainScreen.Overview.route) {
            OverviewScreen()
        }

        composable(MainScreen.Database.route) {
            DatabaseEntryScreen()
        }

        composable(MainScreen.Ocr.route) {
            OcrEntryScreen()
        }

        composable(MainScreen.Settings.route) {
            SettingsScreen()
        }
    }
}
