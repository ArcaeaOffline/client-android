package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.mainNavControllerNavigateToRoute
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabaseEntryScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.OcrEntryScreen
import xyz.sevive.arcaeaoffline.ui.screens.overview.OverviewScreen
import xyz.sevive.arcaeaoffline.ui.screens.resources.ResourcesScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.SettingsScreen
import xyz.sevive.arcaeaoffline.ui.screens.utilities.UtilitiesEntryScreen

const val OVERVIEW_NAV_ROUTE_ROOT = "overview"

enum class MainScreen(
    val route: String,
    val icon: @Composable () -> ImageVector,
    @StringRes val title: Int,
) {
    Overview(
        OVERVIEW_NAV_ROUTE_ROOT,
        { Icons.Filled.Dashboard },
        R.string.nav_home,
    ),
    Database(
        DATABASE_NAV_ROUTE_ROOT,
        { Icons.Filled.MusicNote },
        R.string.nav_plays,
    ),
    Resources(
        RESOURCES_NAV_ROUTE_ROOT,
        { ImageVector.vectorResource(R.drawable.ic_database) },
        R.string.nav_resources,
    ),
    Utilities(
        UTILITIES_NAV_ROUTE_ROOT,
        { Icons.Filled.HomeRepairService },
        R.string.nav_tools,
    ),
    Ocr(
        OCR_NAV_ROUTE_ROOT,
        { ImageVector.vectorResource(R.drawable.ic_ocr) },
        R.string.nav_ocr,
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
            OverviewScreen(
                onNavigateToSettings = {
                    mainNavController.navigate(SETTINGS_NAV_ROUTE_ROOT) { launchSingleTop = true }
                },
                onNavigateToResources = {
                    mainNavControllerNavigateToRoute(mainNavController, MainScreen.Resources.route)
                },
            )
        }

        composable(MainScreen.Database.route) {
            DatabaseEntryScreen()
        }

        composable(MainScreen.Resources.route) {
            ResourcesScreen()
        }

        composable(MainScreen.Utilities.route) {
            UtilitiesEntryScreen()
        }

        composable(MainScreen.Ocr.route) {
            OcrEntryScreen(
                onNavigateToResources = {
                    // A cross-tab jump from the OCR hints, this behavior keeps OCR page on the back stack.
                    mainNavController.navigate(MainScreen.Resources.route) { launchSingleTop = true }
                },
            )
        }

        composable(SETTINGS_NAV_ROUTE_ROOT) {
            SettingsScreen(
                onNavigateBack = { mainNavController.popBackStack() },
            )
        }
    }
}
