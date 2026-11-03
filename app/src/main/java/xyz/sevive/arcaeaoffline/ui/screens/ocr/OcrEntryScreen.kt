package xyz.sevive.arcaeaoffline.ui.screens.ocr

import androidx.compose.runtime.Composable
import xyz.sevive.arcaeaoffline.ui.AdaptiveEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.OcrSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.performance.OcrPerformanceScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreen

@Composable
fun OcrEntryScreen(
    initialDetailRoute: String? = null,
    onNavigateToResources: () -> Unit,
) = AdaptiveEntryScreen(
    initialDetailRoute = initialDetailRoute,
    listPane = { OcrNavEntry() },
    detailPane = { route ->
        when (route) {
            OcrSubScreen.Dependencies.route -> OcrDependenciesScreen()
            OcrSubScreen.Queue.route -> OcrQueueScreen(onNavigateToResources = onNavigateToResources)
            OcrSubScreen.Performance.route -> OcrPerformanceScreen()
        }
    },
)
