package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.database.DatabaseEntryScreen
import xyz.sevive.arcaeaoffline.ui.ocr.OcrEntryScreen
import xyz.sevive.arcaeaoffline.ui.overview.OverviewScreen
import xyz.sevive.arcaeaoffline.ui.settings.SettingsScreen


private fun mainNavControllerNavigateToRoute(navController: NavController, route: String) {
    navController.navigate(route) {
        navController.graph.startDestinationRoute?.let { screenRoute ->
            popUpTo(screenRoute) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun MainNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRouteRoot = currentRoute?.split("/")

    NavigationBar(containerColor = MaterialTheme.colorScheme.inverseOnSurface) {
        MainScreen.entries.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon(), null) },
                label = { Text(stringResource(item.title)) },
                alwaysShowLabel = true,
                selected = if (currentRouteRoot != null) currentRouteRoot[0] == item.route else false,
                onClick = { mainNavControllerNavigateToRoute(navController, item.route) },
            )
        }
    }
}


@Composable
fun MainNavigationRail(navController: NavController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRouteRoot = currentRoute?.split("/")

    NavigationRail(modifier, containerColor = MaterialTheme.colorScheme.inverseOnSurface) {
        Spacer(Modifier.weight(1f))

        MainScreen.entries.forEach { item ->
            NavigationRailItem(
                icon = { Icon(item.icon(), null) },
                modifier = Modifier.padding(
                    0.dp, dimensionResource(R.dimen.general_icon_text_padding)
                ),
                label = { Text(stringResource(item.title)) },
                alwaysShowLabel = true,
                selected = if (currentRouteRoot != null) currentRouteRoot[0] == item.route else false,
                onClick = { mainNavControllerNavigateToRoute(navController, item.route) },
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

enum class MainScreenNavigationType {
    BAR, RAIL;

    companion object {
        fun getNavigationType(windowSizeClass: WindowSizeClass): MainScreenNavigationType {
            return if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded) {
                RAIL
            } else {
                BAR
            }
        }
    }
}

enum class MainScreen(
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
    NavHost(mainNavController, startDestination = MainScreen.Overview.route, modifier = modifier) {
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
