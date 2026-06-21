package xyz.sevive.arcaeaoffline.ui.screens.utilities

import androidx.compose.runtime.Composable
import xyz.sevive.arcaeaoffline.ui.AdaptiveEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.UtilitiesSubScreen

@Composable
fun UtilitiesEntryScreen() {
    AdaptiveEntryScreen(
        listPane = { UtilitiesNavEntry() },
        detailPane = { route ->
            when (route) {
                UtilitiesSubScreen.Calculator.route -> UtilitiesCalculatorScreen()
            }
        },
    )
}
