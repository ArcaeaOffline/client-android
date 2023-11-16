package xyz.sevive.arcaeaoffline

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import xyz.sevive.arcaeaoffline.ui.navigation.MainNavigationGraph
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreens
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
fun ArcaeaOfflineNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRouteRoot = currentRoute?.split("/")

    NavigationBar {
        MainScreens.values().forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon(), null) },
                label = { Text(stringResource(item.title)) },
                alwaysShowLabel = true,
                selected = if (currentRouteRoot != null) currentRouteRoot[0] == item.route else false,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { screenRoute ->
                            popUpTo(screenRoute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Composable
fun ArcaeaOfflineNavigationRail(navController: NavController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRouteRoot = currentRoute?.split("/")

    NavigationRail(modifier) {
        Spacer(Modifier.weight(1f))

        MainScreens.values().forEach { item ->
            NavigationRailItem(
                icon = { Icon(item.icon(), null) },
                modifier = Modifier.padding(
                    0.dp, dimensionResource(R.dimen.general_icon_text_padding)
                ),
                label = { Text(stringResource(item.title)) },
                alwaysShowLabel = true,
                selected = if (currentRouteRoot != null) currentRouteRoot[0] == item.route else false,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { screenRoute ->
                            popUpTo(screenRoute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun ArcaeaOfflineMainScreen(windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded) {
        Row(Modifier.fillMaxHeight()) {
            ArcaeaOfflineNavigationRail(navController, Modifier.fillMaxHeight())
            MainNavigationGraph(navController)
        }
    } else {
        Scaffold(
            bottomBar = { ArcaeaOfflineNavigationBar(navController) },
        ) { padding ->
            Surface(modifier.padding(padding)) {
                MainNavigationGraph(navController = navController)
            }
        }
    }
}

@Preview
@Composable
private fun ArcaeaOfflineBottomNavigationBarPreview(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    ArcaeaOfflineTheme {
        ArcaeaOfflineNavigationBar(navController = navController)
    }
}
