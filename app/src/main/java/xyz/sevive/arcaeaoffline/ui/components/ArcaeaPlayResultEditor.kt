package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.components.arcaea.OutlinedArcaeaScoreTextField
import xyz.sevive.arcaeaoffline.ui.components.arcaea.rememberArcaeaScoreTextFieldState
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton

private val playResultSaver: Saver<PlayResult, String> =
    Saver(
        save = { Json.encodeToString(it) },
        restore = { Json.decodeFromString(it) },
    )

@Composable
fun ArcaeaPlayResultEditorContent(
    playResult: PlayResult,
    onPlayResultChange: (PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs =
        listOf(
            R.string.play_result_editor_main_fields,
            R.string.play_result_editor_other_fields,
        )

    val playResultScoreTextFieldState = rememberArcaeaScoreTextFieldState(playResult.score)
    LaunchedEffect(playResultScoreTextFieldState.intValue) {
        playResultScoreTextFieldState.intValue?.let {
            onPlayResultChange(playResult.copy(score = it))
        }
    }

    Column(modifier = modifier) {
        PrimaryTabRow(selectedTabIndex = tabIndex, containerColor = Color.Transparent) {
            tabs.forEachIndexed { index, titleRId ->
                Tab(
                    text = { Text(stringResource(titleRId)) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                )
            }
        }

        LazyColumn {
            when (tabIndex) {
                0 -> {
                    item {
                        OutlinedArcaeaScoreTextField(
                            playResultScoreTextFieldState,
                            Modifier.fillMaxWidth(),
                        )
                    }

                    item {
                        PlayResultEditorPureField(
                            pure = playResult.pure,
                            onPureChange = { onPlayResultChange(playResult.copy(pure = it)) },
                        )
                    }
                    item {
                        PlayResultEditorFarField(
                            far = playResult.far,
                            onFarChange = { onPlayResultChange(playResult.copy(far = it)) },
                        )
                    }
                    item {
                        PlayResultEditorLostField(
                            lost = playResult.lost,
                            onLostChange = { onPlayResultChange(playResult.copy(lost = it)) },
                        )
                    }

                    item {
                        PlayResultEditorDateTimeField(
                            instant = playResult.date,
                            onInstantChange = { onPlayResultChange(playResult.copy(date = it)) },
                        )
                    }

                    item {
                        PlayResultEditorMaxRecallField(
                            maxRecall = playResult.maxRecall,
                            onMaxRecallChange = { onPlayResultChange(playResult.copy(maxRecall = it)) },
                        )
                    }
                }

                1 -> {
                    item {
                        PlayResultEditorModifierField(
                            arcaeaModifier = playResult.modifier,
                            onArcaeaModifierChange = { onPlayResultChange(playResult.copy(modifier = it)) },
                        )
                    }

                    item {
                        PlayResultEditorClearTypeField(
                            clearType = playResult.clearType,
                            onClearTypeChange = { onPlayResultChange(playResult.copy(clearType = it)) },
                        )
                    }

                    item {
                        PlayResultEditorCommentField(
                            comment = playResult.comment,
                            onCommentChange = { onPlayResultChange(playResult.copy(comment = it)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArcaeaPlayResultEditorDialog(
    onDismiss: () -> Unit,
    playResult: PlayResult,
    onPlayResultChange: (PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var playResultCache by rememberSaveable(stateSaver = playResultSaver) {
        mutableStateOf(playResult)
    }

    AlertDialog(
        onDismissRequest = {},
        modifier = modifier,
        text = {
            ArcaeaPlayResultEditorContent(
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
