package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultEditorDialog
import xyz.sevive.arcaeaoffline.ui.components.rememberArcaeaChartDisplay

@Composable
internal fun OcrFromShareOcrResult(viewModel: OcrFromShareViewModel) {
    val context = LocalContext.current

    val score by viewModel.playResult.collectAsStateWithLifecycle()
    val exception by viewModel.exception.collectAsStateWithLifecycle()
    val display by rememberArcaeaChartDisplay(score?.songId, score?.ratingClass)

    val warnings =
        remember(score, display) {
            score?.let { ArcaeaPlayResultValidator.validate(playResult = it, chartInfo = display?.chartInfo) }.orEmpty()
        }

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
                difficultyWithSong = display?.difficultyWithSong,
                chartInfo = display?.chartInfo,
                warnings = warnings,
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
        Column {
            Text(exception.toString(), color = MaterialTheme.colorScheme.error)

            TextButton(onClick = { viewModel.retryOcr(context) }) {
                Text(stringResource(R.string.ocr_from_share_retry_ocr))
            }
        }
    } else {
        Text(stringResource(R.string.ocr_from_share_waiting_result))
    }
}
