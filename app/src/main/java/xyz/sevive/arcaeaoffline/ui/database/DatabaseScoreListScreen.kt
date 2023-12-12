package xyz.sevive.arcaeaoffline.ui.database

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreCalculated
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditor
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditorViewModel
import xyz.sevive.arcaeaoffline.ui.utils.potentialToText

@Composable
internal fun DatabaseScoreListItem(
    score: Score,
    onRequestEdit: () -> Unit,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier,
    scoreCalculated: ScoreCalculated? = null,
) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        ArcaeaScoreCard(score = score, Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PTT", style = MaterialTheme.typography.labelSmall)

            Text(
                potentialToText(scoreCalculated?.potential),
                style = MaterialTheme.typography.labelMedium,
            )

            IconButton(onClick = onRequestEdit) {
                Icon(Icons.Default.Edit, null)
            }
            IconButton(onClick = onRequestDelete) {
                Icon(Icons.Default.Delete, null)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DatabaseScoreListScreen(
    onNavigateUp: () -> Unit,
    databaseScoreListViewModel: DatabaseScoreListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scoreEditorViewModel: ScoreEditorViewModel = viewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val scoreList by databaseScoreListViewModel.scoreList.collectAsState()
    val scoreCalculatedList by databaseScoreListViewModel.scoreCalculatedList.collectAsState()
    var showScoreEditor by rememberSaveable { mutableStateOf(false) }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = { Text(stringResource(R.string.database_score_list_title)) },
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
            items(scoreList, key = { it.id }) {
                DatabaseScoreListItem(
                    score = it,
                    {
                        scoreEditorViewModel.setArcaeaScore(it)
                        showScoreEditor = true
                    },
                    {
                        coroutineScope.launch {
                            databaseScoreListViewModel.deleteScore(it)
                        }
                        Toast.makeText(context, "Delete score ${it.id}", Toast.LENGTH_SHORT).show()
                    },
                    // this makes list item jitter when positioned near the top/bottom
                    // TODO: issue in this ExperimentalApi or `ArcaeaScoreCard`?
                    Modifier.animateItemPlacement(),
                    scoreCalculatedList.find { sc -> sc.id == it.id },
                )
            }
        }
    }

    if (showScoreEditor) {
        Dialog(
            onDismissRequest = { showScoreEditor = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(Modifier.padding(dimensionResource(R.dimen.general_page_padding))) {
                ScoreEditor(
                    onScoreCommit = {
                        coroutineScope.launch {
                            databaseScoreListViewModel.updateScore(it)
                        }
                        Toast.makeText(
                            context, "Update score ${it.id}", Toast.LENGTH_SHORT
                        ).show()
                        showScoreEditor = false
                    },
                    scoreEditorViewModel,
                )
            }
        }
    }
}
