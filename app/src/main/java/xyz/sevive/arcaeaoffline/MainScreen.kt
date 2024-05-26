package xyz.sevive.arcaeaoffline

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import xyz.sevive.arcaeaoffline.ui.navigation.MainNavigationGraph
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreenDestinations


internal fun mainNavControllerNavigateToRoute(navController: NavController, route: String) {
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
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRouteRoot = currentRoute?.split("/")

    val navigationSuiteColors = NavigationSuiteDefaults.colors(
        navigationBarContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
        navigationRailContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MainScreenDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon(), contentDescription = null) },
                    label = { Text(stringResource(it.title)) },
                    alwaysShowLabel = true,
                    selected = if (currentRouteRoot != null) currentRouteRoot[0] == it.route else false,
                    onClick = { mainNavControllerNavigateToRoute(navController, it.route) },
                )
            }
        },
        modifier = modifier,
        navigationSuiteColors = navigationSuiteColors,
    ) {
        MainNavigationGraph(navController)
    }
}
