package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R

const val OcrNavRouteRoot = "ocr"

enum class OcrScreenDestinations(val route: String, @StringRes val title: Int) {
    Dependencies("$OcrNavRouteRoot/dependencies", R.string.ocr_dependencies_title),
    Queue("$OcrNavRouteRoot/queue", R.string.ocr_queue_title),
}
