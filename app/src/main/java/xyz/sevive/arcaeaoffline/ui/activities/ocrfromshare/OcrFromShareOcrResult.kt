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
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditorDialog


@Composable
internal fun OcrFromShareOcrResult(viewModel: OcrFromShareViewModel) {
    val score by viewModel.score.collectAsState()
    val exception by viewModel.exception.collectAsState()
    val chart by viewModel.chart.collectAsState()

    var showScoreEditorDialog by rememberSaveable { mutableStateOf(false) }

    if (showScoreEditorDialog && score != null) {
        ScoreEditorDialog(
            onDismiss = { showScoreEditorDialog = false },
            score = score!!,
            onScoreChange = { viewModel.setScore(it) },
        )
    }

    val scoreSaved by viewModel.scoreSaved.collectAsState()
    val scoreCached by viewModel.scoreCached.collectAsState()

    if (score != null) {
        Row(verticalAlignment = Alignment.Bottom) {
            ArcaeaScoreCard(score = score!!, modifier = Modifier.weight(1f), chart = chart)

            AnimatedVisibility(visible = !scoreSaved && !scoreCached) {
                IconButton(onClick = { showScoreEditorDialog = true }) {
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
