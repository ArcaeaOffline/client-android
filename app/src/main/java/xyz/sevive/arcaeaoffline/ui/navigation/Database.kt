package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R

const val DATABASE_NAV_ROUTE_ROOT = "database"

enum class DatabaseSubScreen(
    val route: String,
    @StringRes val title: Int,
) {
    Manage("$DATABASE_NAV_ROUTE_ROOT/manage", R.string.database_manage_title),
    AddPlayResult("$DATABASE_NAV_ROUTE_ROOT/add_play_result", R.string.database_add_play_result_title),
    ScoreList("$DATABASE_NAV_ROUTE_ROOT/score_list", R.string.database_play_result_list_title),
    B30("$DATABASE_NAV_ROUTE_ROOT/b30_list", R.string.database_b30_list_title),
    R30("$DATABASE_NAV_ROUTE_ROOT/r30_list", R.string.database_r30_list_title),
    Deduplicator("$DATABASE_NAV_ROUTE_ROOT/deduplicator", R.string.database_deduplicator_title),
}
