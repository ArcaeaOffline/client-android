package xyz.sevive.arcaeaoffline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import xyz.sevive.arcaeaoffline.ui.navigation.MainNavigationBar
import xyz.sevive.arcaeaoffline.ui.navigation.MainNavigationGraph
import xyz.sevive.arcaeaoffline.ui.navigation.MainNavigationRail
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreenNavigationType

@Composable
fun MainScreen(windowSizeClass: WindowSizeClass, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navigationType = MainScreenNavigationType.getNavigationType(windowSizeClass)

    Row(modifier.fillMaxSize()) {
        AnimatedVisibility(navigationType == MainScreenNavigationType.RAIL) {
            MainNavigationRail(navController, Modifier.fillMaxHeight())
        }

        Column(Modifier.fillMaxSize()) {
            MainNavigationGraph(navController, Modifier.weight(1f))
            AnimatedVisibility(navigationType == MainScreenNavigationType.BAR) {
                MainNavigationBar(navController)
            }
        }
    }
}
