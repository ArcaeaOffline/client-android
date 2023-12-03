package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard

@Composable
fun DatabaseScoreListScreen(
    onNavigateUp: () -> Unit,
    viewModel: DatabaseScoreListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scoreList by viewModel.scoreList.collectAsState()

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = { Text(stringResource(R.string.database_score_list_title)) },
    ) {
        LazyColumn {
            items(scoreList.size) {
                ArcaeaScoreCard(score = scoreList[it])
            }
        }
    }
}
