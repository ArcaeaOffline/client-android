package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R

const val DatabaseNavRouteRoot = "database"

enum class DatabaseScreenDestinations(val route: String, @StringRes val title: Int) {
    Manage("$DatabaseNavRouteRoot/manage", R.string.database_manage_title),
    AddScore("$DatabaseNavRouteRoot/add_score", R.string.database_add_play_result_title),
    ScoreList("$DatabaseNavRouteRoot/score_list", R.string.database_play_result_list_title),
    B30("$DatabaseNavRouteRoot/b30_list", R.string.database_b30_list_title),
}
