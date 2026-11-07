package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartSelector
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultEditorDialog
import xyz.sevive.arcaeaoffline.ui.components.BasicAlertDialogSurface
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.helpers.DifficultyWithSongDisplay
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import kotlin.time.Instant

@Composable
internal fun OcrFromShareOcrResult(
    playResult: PlayResult?,
    onSetPlayResult: (PlayResult) -> Unit,
    display: DifficultyWithSongDisplay?,
    warnings: List<ArcaeaPlayResultValidatorWarning>,
    playResultSaveState: OcrFromShareViewModel.PlayResultSaveState,
    exception: Exception?,
    onRetryOcr: () -> Unit,
) {
    val playResultSaved = playResultSaveState.saved
    val playResultCached = playResultSaveState.cached
    var showPlayResultEditorDialog by rememberSaveable { mutableStateOf(false) }

    if (showPlayResultEditorDialog && playResult != null) {
        ArcaeaPlayResultEditorDialog(
            onDismiss = { showPlayResultEditorDialog = false },
            playResult = playResult,
            onPlayResultChange = onSetPlayResult,
        )
    }

    var showChartSelector by rememberSaveable { mutableStateOf(false) }

    if (showChartSelector && playResult != null) {
        BasicAlertDialogSurface(onDismissRequest = { showChartSelector = false }) {
            ArcaeaChartSelector(
                songId = playResult.songId,
                ratingClass = playResult.ratingClass,
                onDifficultyChange = {
                    onSetPlayResult(playResult.copy(songId = it.songId, ratingClass = it.ratingClass))
                    // do not close dialog here since the selector will fire its initial selection
                    // when the dialog opens
                },
            )
        }
    }

    // Size changes between the waiting, error, and result branches animate instead of jumping.
    Box(Modifier.animateContentSize()) {
        if (playResult != null) {
            Row(verticalAlignment = Alignment.Bottom) {
                ArcaeaPlayResultCard(
                    playResult = playResult,
                    modifier = Modifier.weight(1f),
                    difficultyWithSong = display?.difficultyWithSong,
                    chartInfo = display?.chartInfo,
                    warnings = warnings,
                )

                AnimatedVisibility(visible = !playResultSaved && !playResultCached) {
                    Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                        var showEditMenu by rememberSaveable { mutableStateOf(false) }
                        IconButton(onClick = { showEditMenu = true }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }

                        DropdownMenu(expanded = showEditMenu, onDismissRequest = { showEditMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.arcaea_play_result)) },
                                onClick = {
                                    showPlayResultEditorDialog = true
                                    showEditMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.arcaea_chart)) },
                                onClick = {
                                    showChartSelector = true
                                    showEditMenu = false
                                },
                            )
                        }
                    }
                }
            }
        } else if (exception != null) {
            Column {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.error,
                ) {
                    Text(
                        stringResource(R.string.ocr_from_share_error),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(exception.toString())
                }

                Spacer(Modifier.height(MaterialTheme.spacing.sm))

                FilledTonalButton(onClick = onRetryOcr) {
                    IconRow {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Text(stringResource(R.string.ocr_from_share_retry_ocr))
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs)) {
                Text(stringResource(R.string.ocr_from_share_waiting_result))
                LinearProgressIndicator(gapSize = 0.dp)
            }
        }
    }
}

private fun previewPlayResult(): PlayResult =
    PlayResult(
        songId = "song_id",
        ratingClass = ArcaeaRatingClass.PRESENT,
        score = 10_001_230,
        pure = 1284,
        far = 0,
        lost = 0,
        maxRecall = 1234,
        date = Instant.fromEpochSeconds(616),
        clearType = ArcaeaPlayResultClearType.FULL_RECALL,
        modifier = ArcaeaPlayResultModifier.HARD,
    )

private fun previewDisplay(): DifficultyWithSongDisplay =
    DifficultyWithSongDisplay(
        difficultyWithSong =
            DifficultyWithSong(
                songId = "song_id",
                ratingClass = ArcaeaRatingClass.PRESENT,
                rating = 12,
                ratingPlus = true,
                title = "Test if Y",
                artist = "void mian();",
            ),
        chartInfo =
            ChartInfo(
                songId = "song_id",
                ratingClass = ArcaeaRatingClass.PRESENT,
                constant = 129,
                notes = 1234,
            ),
    )

@Preview(name = "OCR success", showBackground = true)
@Composable
private fun PreviewOcrResultWithScore() {
    ArcaeaOfflineTheme {
        Surface {
            OcrFromShareOcrResult(
                playResult = previewPlayResult(),
                display = previewDisplay(),
                warnings = emptyList(),
                exception = null,
                playResultSaveState =
                    OcrFromShareViewModel.PlayResultSaveState(
                        saved = false,
                        cached = false,
                    ),
                onSetPlayResult = {},
                onRetryOcr = {},
            )
        }
    }
}

@Preview(name = "OCR success & saved", showBackground = true)
@Composable
private fun PreviewOcrResultScoreSaved() {
    ArcaeaOfflineTheme {
        Surface {
            OcrFromShareOcrResult(
                playResult = previewPlayResult(),
                display = previewDisplay(),
                warnings = emptyList(),
                exception = null,
                playResultSaveState =
                    OcrFromShareViewModel.PlayResultSaveState(
                        saved = true,
                        cached = false,
                    ),
                onSetPlayResult = {},
                onRetryOcr = {},
            )
        }
    }
}

@Preview(name = "OCR error", showBackground = true, heightDp = 200)
@Composable
private fun PreviewOcrResultError() {
    ArcaeaOfflineTheme {
        Surface {
            OcrFromShareOcrResult(
                playResult = null,
                display = null,
                warnings = emptyList(),
                exception = IllegalStateException("Wow error"),
                playResultSaveState =
                    OcrFromShareViewModel.PlayResultSaveState(
                        saved = false,
                        cached = false,
                    ),
                onSetPlayResult = {},
                onRetryOcr = {},
            )
        }
    }
}

@Preview(name = "OCR waiting", showBackground = true, heightDp = 120)
@Composable
private fun PreviewOcrResultWaiting() {
    ArcaeaOfflineTheme {
        Surface {
            OcrFromShareOcrResult(
                playResult = null,
                display = null,
                warnings = emptyList(),
                exception = null,
                playResultSaveState =
                    OcrFromShareViewModel.PlayResultSaveState(
                        saved = false,
                        cached = false,
                    ),
                onSetPlayResult = {},
                onRetryOcr = {},
            )
        }
    }
}
