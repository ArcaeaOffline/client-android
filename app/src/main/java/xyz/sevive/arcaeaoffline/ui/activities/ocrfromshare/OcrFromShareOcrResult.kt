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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultEditorDialog


@Composable
internal fun OcrFromShareOcrResult(viewModel: OcrFromShareViewModel) {
    val score by viewModel.score.collectAsStateWithLifecycle()
    val exception by viewModel.exception.collectAsStateWithLifecycle()
    val chart by viewModel.chart.collectAsStateWithLifecycle()

    var showScoreEditorDialog by rememberSaveable { mutableStateOf(false) }

    if (showScoreEditorDialog && score != null) {
        ArcaeaPlayResultEditorDialog(
            onDismiss = { showScoreEditorDialog = false },
            playResult = score!!,
            onPlayResultChange = { viewModel.setScore(it) },
        )
    }

    val scoreSaved by viewModel.scoreSaved.collectAsStateWithLifecycle()
    val scoreCached by viewModel.scoreCached.collectAsStateWithLifecycle()

    if (score != null) {
        Row(verticalAlignment = Alignment.Bottom) {
            ArcaeaPlayResultCard(
                playResult = score!!,
                modifier = Modifier.weight(1f),
                chart = chart
            )

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
