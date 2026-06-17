package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R

const val OCR_NAV_ROUTE_ROOT = "ocr"

enum class OcrSubScreen(
    val route: String,
    @StringRes val title: Int,
) {
    Dependencies("$OCR_NAV_ROUTE_ROOT/dependencies", R.string.ocr_dependencies_title),
    Queue("$OCR_NAV_ROUTE_ROOT/queue", R.string.ocr_queue_title),
}
