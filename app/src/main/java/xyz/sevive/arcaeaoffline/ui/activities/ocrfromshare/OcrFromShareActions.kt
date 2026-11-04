package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorScoreIsZeroWarning
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.ui.components.SaveWithWarningsDialog
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
internal fun OcrFromShareActions(
    hasPlayResult: Boolean,
    playResultSaveState: OcrFromShareViewModel.PlayResultSaveState,
    warnings: List<ArcaeaPlayResultValidatorWarning>,
    onSavePlayResult: () -> Unit,
    onCachePlayResult: () -> Unit,
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

    // The whole row morphs into a single saved segment once the play result is saved.
    AnimatedContent(playResultSaved) { saved ->
        if (saved) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                // Selected and disabled renders through the disabled-active colors
                // The alpha-faded defaults would wash the primary out
                SegmentedButton(
                    selected = true,
                    onClick = {},
                    enabled = false,
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 1),
                    colors =
                        SegmentedButtonDefaults.colors(
                            disabledActiveContainerColor = Color.Transparent,
                            disabledActiveBorderColor = MaterialTheme.colorScheme.primary,
                            disabledActiveContentColor = MaterialTheme.colorScheme.primary,
                        ),
                    label = { Text(stringResource(R.string.ocr_from_share_play_result_saved)) },
                )
            }
        } else {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = false,
                    onClick = {
                        if (warnings.isEmpty()) {
                            onSavePlayResult()
                        } else {
                            showSaveWithWarningsDialog = true
                        }
                    },
                    enabled = canSavePlayResult,
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors =
                        SegmentedButtonDefaults.colors(
                            inactiveContainerColor = MaterialTheme.colorScheme.primary,
                            inactiveContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    icon = { Icon(Icons.Default.Save, null) },
                    label = { Text(stringResource(R.string.general_save)) },
                )

                SegmentedButton(
                    selected = false,
                    onClick = onCachePlayResult,
                    enabled = canSavePlayResult,
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.Archive, null) },
                    label = { Text(stringResource(R.string.ocr_from_share_cache_play_result_button)) },
                )
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
                warnings = listOf(ArcaeaPlayResultValidatorScoreIsZeroWarning),
                onSavePlayResult = {},
                onCachePlayResult = {},
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
                warnings = emptyList(),
                onSavePlayResult = {},
                onCachePlayResult = {},
            )
        }
    }
}
