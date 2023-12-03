package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R

const val DatabaseNavRouteRoot = "database"

enum class DatabaseScreens(val route: String, @StringRes val title: Int) {
    Empty("$DatabaseNavRouteRoot/empty", R.string.develop_placeholder),
    Manage("$DatabaseNavRouteRoot/manage", R.string.database_manage_title),
    AddScore("$DatabaseNavRouteRoot/add_score", R.string.database_add_score_title),
    ScoreList("$DatabaseNavRouteRoot/score_list", R.string.database_score_list_title),
}
