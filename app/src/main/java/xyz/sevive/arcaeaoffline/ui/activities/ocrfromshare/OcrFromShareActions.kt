package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorScoreIsZeroWarning
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.SaveWithWarningsDialog
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
internal fun OcrFromShareActions(
    hasPlayResult: Boolean,
    playResultSaveState: OcrFromShareViewModel.PlayResultSaveState,
    imageSaved: Boolean,
    warnings: List<ArcaeaPlayResultValidatorWarning>,
    onSavePlayResult: () -> Unit,
    onCachePlayResult: () -> Unit,
    onSaveImage: () -> Unit,
) {
    val playResultSaved = playResultSaveState.saved
    val playResultCached = playResultSaveState.cached
    val canSavePlayResult = hasPlayResult && !playResultSaved && !playResultCached

    var showSaveWithWarningsDialog by rememberSaveable { mutableStateOf(false) }
    if (showSaveWithWarningsDialog) {
        SaveWithWarningsDialog(
            warnings = warnings,
            onConfirm = {
                showSaveWithWarningsDialog = false
                onSavePlayResult()
            },
            onDismissRequest = { showSaveWithWarningsDialog = false },
        )
    }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            Button(
                onClick = {
                    if (warnings.isEmpty()) {
                        onSavePlayResult()
                    } else {
                        showSaveWithWarningsDialog = true
                    }
                },
                enabled = canSavePlayResult,
            ) {
                AnimatedContent(playResultSaved) {
                    if (it) {
                        IconRow {
                            Icon(Icons.Default.Check, null)
                            Text(stringResource(R.string.ocr_from_share_play_result_saved))
                        }
                    } else {
                        IconRow {
                            Icon(Icons.Default.Save, null)
                            Text(stringResource(R.string.general_save))
                        }
                    }
                }
            }

            OutlinedButton(onClick = onCachePlayResult, enabled = canSavePlayResult) {
                IconRow {
                    Icon(Icons.Default.Archive, null)
                    Text(stringResource(R.string.ocr_from_share_cache_play_result_button))
                }
            }
        }

        OutlinedButton(
            onClick = onSaveImage,
            enabled = !imageSaved,
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary,
                ),
        ) {
            AnimatedContent(imageSaved) {
                if (it) {
                    IconRow {
                        Icon(Icons.Default.Check, null)
                        Text(stringResource(R.string.ocr_from_share_image_saved))
                    }
                } else {
                    IconRow {
                        Icon(Icons.Default.FileDownload, null)
                        Text(stringResource(R.string.ocr_from_share_save_image_button))
                    }
                }
            }
        }
    }
}

@Preview(name = "Actions", showBackground = true)
@Composable
private fun PreviewActions() {
    ArcaeaOfflineTheme {
        Surface {
            OcrFromShareActions(
                hasPlayResult = true,
                playResultSaveState =
                    OcrFromShareViewModel.PlayResultSaveState(
                        saved = false,
                        cached = false,
                    ),
                imageSaved = false,
                warnings = listOf(ArcaeaPlayResultValidatorScoreIsZeroWarning),
                onSavePlayResult = {},
                onCachePlayResult = {},
                onSaveImage = {},
            )
        }
    }
}

@Preview(name = "Actions saved", showBackground = true)
@Composable
private fun PreviewActionsSaved() {
    ArcaeaOfflineTheme {
        Surface {
            OcrFromShareActions(
                hasPlayResult = true,
                playResultSaveState =
                    OcrFromShareViewModel.PlayResultSaveState(
                        saved = true,
                        cached = false,
                    ),
                imageSaved = true,
                warnings = emptyList(),
                onSavePlayResult = {},
                onCachePlayResult = {},
                onSaveImage = {},
            )
        }
    }
}
