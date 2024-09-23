package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton


private val playResultSaver: Saver<PlayResult, String> = Saver(
    save = { Json.encodeToString(it) },
    restore = { Json.decodeFromString(it) },
)

@Composable
fun PlayResultEditorContent(
    playResult: PlayResult,
    onPlayResultChange: (PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        R.string.play_result_editor_main_fields,
        R.string.play_result_editor_other_fields,
    )

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = tabIndex, containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, titleRId ->
                Tab(
                    text = { Text(stringResource(titleRId)) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                )
            }
        }

        when (tabIndex) {
            0 -> {
                PlayResultEditorScoreField(
                    score = playResult.score,
                    onScoreChange = { onPlayResultChange(playResult.copy(score = it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                PlayResultEditorPureField(
                    pure = playResult.pure,
                    onPureChange = { onPlayResultChange(playResult.copy(pure = it)) },
                )
                PlayResultEditorFarField(
                    far = playResult.far,
                    onFarChange = { onPlayResultChange(playResult.copy(far = it)) },
                )
                PlayResultEditorLostField(
                    lost = playResult.lost,
                    onLostChange = { onPlayResultChange(playResult.copy(lost = it)) },
                )

                PlayResultEditorDateTimeField(
                    instant = playResult.date,
                    onInstantChange = { onPlayResultChange(playResult.copy(date = it)) },
                )

                PlayResultEditorMaxRecallField(
                    maxRecall = playResult.maxRecall,
                    onMaxRecallChange = { onPlayResultChange(playResult.copy(maxRecall = it)) },
                )
            }

            1 -> {
                PlayResultEditorModifierField(
                    arcaeaModifier = playResult.modifier,
                    onArcaeaModifierChange = { onPlayResultChange(playResult.copy(modifier = it)) },
                )

                PlayResultEditorClearTypeField(
                    clearType = playResult.clearType,
                    onClearTypeChange = { onPlayResultChange(playResult.copy(clearType = it)) },
                )

                PlayResultEditorCommentField(
                    comment = playResult.comment,
                    onCommentChange = { onPlayResultChange(playResult.copy(comment = it)) },
                )
            }
        }
    }
}

@Composable
fun PlayResultEditorDialog(
    onDismiss: () -> Unit,
    playResult: PlayResult,
    onPlayResultChange: (PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var playResultCache by rememberSaveable(stateSaver = playResultSaver) {
        mutableStateOf(playResult)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        text = {
            PlayResultEditorContent(
                playResult = playResultCache,
                onPlayResultChange = { playResultCache = it },
            )
        },
        confirmButton = {
            DialogConfirmButton(
                onClick = {
                    onPlayResultChange(playResultCache)
                    onDismiss()
                },
            )
        },
        dismissButton = { DialogDismissTextButton(onClick = onDismiss) },
    )
}
