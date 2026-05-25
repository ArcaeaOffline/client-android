package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R

const val SETTINGS_NAV_ROUTE_ROOT = "settings"

enum class SettingsScreenDestination(
    val route: String,
    @StringRes val title: Int,
) {
    General("$SETTINGS_NAV_ROUTE_ROOT/general", R.string.settings_general_title),
    About("$SETTINGS_NAV_ROUTE_ROOT/about", R.string.settings_about_title),
    License("$SETTINGS_NAV_ROUTE_ROOT/license", R.string.settings_license_title),
    Aboutlibraries("$SETTINGS_NAV_ROUTE_ROOT/aboutlibraries", R.string.settings_aboutlibraries_title),
    UnstableAlert("$SETTINGS_NAV_ROUTE_ROOT/unstablealert", R.string.unstable_version_alert_title),
}
