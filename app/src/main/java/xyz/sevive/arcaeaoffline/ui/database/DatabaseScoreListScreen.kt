package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard

@Composable
fun DatabaseScoreListScreen(
    viewModel: DatabaseScoreListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scoreList by viewModel.scoreList.collectAsState()

    LazyColumn {
        items(scoreList.size) {
            ArcaeaScoreCard(score = scoreList[it])
        }
    }
}
