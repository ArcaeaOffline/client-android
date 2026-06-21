package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R

const val UTILITIES_NAV_ROUTE_ROOT = "utilities"

enum class UtilitiesSubScreen(
    val route: String,
    @StringRes val title: Int,
) {
    Calculator("$UTILITIES_NAV_ROUTE_ROOT/calculator", R.string.utilities_calculator_title),
}
