package xyz.sevive.arcaeaoffline.ui.screens.ocr

import androidx.compose.runtime.Composable
import xyz.sevive.arcaeaoffline.ui.AdaptiveEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.OcrScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreen

@Composable
fun OcrEntryScreen() = AdaptiveEntryScreen(
    listPane = { OcrNavEntry() },
    detailPane = { route ->
        when (route) {
            OcrScreenDestinations.Dependencies.route -> OcrDependenciesScreen()
            OcrScreenDestinations.Queue.route -> OcrQueueScreen()
        }
    },
)
