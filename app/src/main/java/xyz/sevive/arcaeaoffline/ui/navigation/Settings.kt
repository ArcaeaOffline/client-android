package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R


const val SettingsNavRouteRoot = "settings"

enum class SettingsScreenDestination(val route: String, @StringRes val title: Int) {
    General("$SettingsNavRouteRoot/general", R.string.settings_general_title),
    About("$SettingsNavRouteRoot/about", R.string.settings_about_title),
    License("$SettingsNavRouteRoot/license", R.string.settings_license_title),
    Aboutlibraries("$SettingsNavRouteRoot/aboutlibraries", R.string.settings_aboutlibraries_title),
    UnstableAlert("$SettingsNavRouteRoot/unstablealert", R.string.unstable_version_alert_title),
}
