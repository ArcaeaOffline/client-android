package xyz.sevive.arcaeaoffline

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import xyz.sevive.arcaeaoffline.ui.screens.DatabaseScreen
import xyz.sevive.arcaeaoffline.ui.screens.OcrScreen
import xyz.sevive.arcaeaoffline.ui.screens.OverviewScreen
import xyz.sevive.arcaeaoffline.ui.screens.SettingsScreen
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

sealed class BottomNavItem(var title: Int, var icon: ImageVector, var screenRoute: String) {
    object Overview : BottomNavItem(R.string.nav_overview, Icons.Filled.Dashboard, "overview")
    object Database : BottomNavItem(R.string.nav_database, Icons.Filled.Storage, "database")
    object Ocr : BottomNavItem(R.string.nav_ocr, Icons.Filled.Image, "ocr")
    object Settings : BottomNavItem(R.string.nav_settings, Icons.Filled.Settings, "settings")
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = BottomNavItem.Overview.screenRoute) {
        composable(BottomNavItem.Overview.screenRoute) {
            OverviewScreen()
        }
        composable(BottomNavItem.Database.screenRoute) {
            DatabaseScreen()
        }
        composable(BottomNavItem.Ocr.screenRoute) {
            OcrScreen()
        }
        composable(BottomNavItem.Settings.screenRoute) {
            SettingsScreen()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Overview,
        BottomNavItem.Database,
        BottomNavItem.Ocr,
        BottomNavItem.Settings,
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(icon = { Icon(item.icon, null) },
                label = { Text(stringResource(item.title)) },
                alwaysShowLabel = true,
                selected = currentRoute == item.screenRoute,
                onClick = {
                    navController.navigate(item.screenRoute) {
                        navController.graph.startDestinationRoute?.let { screenRoute ->
                            popUpTo(screenRoute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
        }
    }
}

@Composable
fun MainScreenView(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavigationBar(navController = navController) }) { padding ->
        Surface(modifier.padding(padding)) {
            NavigationGraph(navController = navController)
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    ArcaeaOfflineTheme {
        BottomNavigationBar(navController = navController)
    }
}
