package xyz.sevive.arcaeaoffline

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import xyz.sevive.arcaeaoffline.ui.navigation.MainNavigationGraph
import xyz.sevive.arcaeaoffline.ui.navigation.RootNavItem
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        RootNavItem.Overview,
        RootNavItem.Database,
        RootNavItem.Ocr,
        RootNavItem.Settings,
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val currentRouteRoot = currentRoute?.split("/")

        items.forEach { item ->
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
                })
        }
    }
}


@Composable
fun MainScreenView(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavigationBar(navController = navController) }) { padding ->
        Surface(modifier.padding(padding)) {
            MainNavigationGraph(navController = navController)
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
