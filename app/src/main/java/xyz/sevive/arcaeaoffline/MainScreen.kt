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
    modifier: Modifier = Modifier,
    initialNavigateRoute: String? = null,
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentRouteRoot = currentRoute?.split("/")

    LaunchedEffect(initialNavigateRoute) {
        val route = initialNavigateRoute ?: return@LaunchedEffect
        val tabRoute = route.substringBefore('/')
        if (MainScreen.entries.none { it.route == tabRoute }) return@LaunchedEffect

        // Navigate with a fresh tab stack:
        // 1. Omit restoreState: prevents restoring a stale nested/detail destination instead of this fresh entry.
        // 2. popUpTo(startRoute, inclusive = true): replaces the initial root so pressing Back exits directly
        //    to the caller activity, while saving the start destination's state for subsequent tab switches.
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
                    selected = currentRouteRoot != null && currentRouteRoot[0] == it.route,
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
