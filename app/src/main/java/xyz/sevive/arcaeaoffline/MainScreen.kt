package xyz.sevive.arcaeaoffline

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import xyz.sevive.arcaeaoffline.ui.navigation.MainNavigationGraph
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreen

internal fun mainNavControllerNavigateToRoute(
    navController: NavController,
    route: String,
) {
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
fun MainScreen(
    initialNavigateRoute: String? = null,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRouteRoot = currentRoute?.split("/")

    LaunchedEffect(initialNavigateRoute) {
        val route = initialNavigateRoute ?: return@LaunchedEffect
        val tabRoute = route.substringBefore('/')
        if (MainScreen.entries.none { it.route == tabRoute }) return@LaunchedEffect

        // A fresh tab entry, not the restoreState helper: a restored entry would bring back
        // its saved detail pane instead of the requested initial one. The start destination
        // is popped (state saved for later tab switches) so back exits the task to the
        // source activity instead of walking through Home.
        val startRoute = navController.graph.startDestinationRoute ?: return@LaunchedEffect
        navController.navigate(tabRoute) {
            popUpTo(startRoute) {
                saveState = true
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val navigationSuiteColors =
        NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
            navigationRailContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
        )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MainScreen.entries.forEach {
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
        MainNavigationGraph(navController, initialNavigateRoute)
    }
}
