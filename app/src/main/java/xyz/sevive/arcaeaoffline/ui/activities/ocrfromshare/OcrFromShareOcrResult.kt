package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.common.scoreeditor.ScoreEditorDialog
import xyz.sevive.arcaeaoffline.ui.common.scoreeditor.ScoreEditorViewModel
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard


@Composable
internal fun OcrFromShareOcrResult(
    ocrFromShareViewModel: OcrFromShareViewModel,
    scoreEditorViewModel: ScoreEditorViewModel = viewModel(),
) {
    val score by ocrFromShareViewModel.score.collectAsState()
    val exception by ocrFromShareViewModel.exception.collectAsState()
    val chart by ocrFromShareViewModel.chart.collectAsState()

    var showScoreEditorDialog by rememberSaveable { mutableStateOf(false) }

    if (showScoreEditorDialog) {
        ScoreEditorDialog(
            onDismiss = { showScoreEditorDialog = false },
            onScoreCommit = { ocrFromShareViewModel.setScore(it) },
            scoreEditorViewModel = scoreEditorViewModel,
        )
    }

    val scoreSaved by ocrFromShareViewModel.scoreSaved.collectAsState()
    val scoreCached by ocrFromShareViewModel.scoreCached.collectAsState()

    if (score != null) {
        Row(verticalAlignment = Alignment.Bottom) {
            ArcaeaScoreCard(score = score!!, modifier = Modifier.weight(1f), chart = chart)

            AnimatedVisibility(visible = !scoreSaved && !scoreCached) {
                IconButton(onClick = {
                    scoreEditorViewModel.setArcaeaScore(score!!)
                    showScoreEditorDialog = true
                }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.general_edit),
                    )
                }
            }
        }
    } else if (exception != null) {
        Text(exception.toString(), color = MaterialTheme.colorScheme.error)
    } else {
        Text(stringResource(R.string.ocr_from_share_waiting_result))
    }
}
