package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

@Composable
fun PlayResultEditor(
    playResult: PlayResult,
    onPlayResultChange: (PlayResult) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayResultEditorViewModel = viewModel()
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs =
        listOf(R.string.play_result_editor_main_fields, R.string.play_result_editor_other_fields)

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, titleRId ->
                Tab(text = { Text(stringResource(titleRId)) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index })
            }
        }

        when (tabIndex) {
            0 -> {
                PlayResultEditorScoreField(score = playResult.score, onScoreChange = {
                    onPlayResultChange(viewModel.editScore(playResult, it))
                }, modifier = Modifier.fillMaxWidth())

                PlayResultEditorPureField(pure = playResult.pure, onPureChange = {
                    onPlayResultChange(viewModel.editPure(playResult, it))
                })
                PlayResultEditorFarField(far = playResult.far, onFarChange = {
                    onPlayResultChange(viewModel.editFar(playResult, it))
                })
                PlayResultEditorLostField(lost = playResult.lost, onLostChange = {
                    onPlayResultChange(viewModel.editLost(playResult, it))
                })

                PlayResultEditorDateTimeField(
                    instant = playResult.date,
                    onInstantChange = {
                        onPlayResultChange(viewModel.editDate(playResult, it))
                    },
                )

                PlayResultEditorMaxRecallField(
                    maxRecall = playResult.maxRecall,
                    onMaxRecallChange = {
                        onPlayResultChange(viewModel.editMaxRecall(playResult, it))
                    })
            }

            1 -> {
                PlayResultEditorModifierField(
                    arcaeaModifier = playResult.modifier,
                    onArcaeaModifierChange = {
                        onPlayResultChange(viewModel.editModifier(playResult, it))
                    })

                PlayResultEditorClearTypeField(
                    clearType = playResult.clearType,
                    onClearTypeChange = {
                        onPlayResultChange(viewModel.editClearType(playResult, it))
                    })

                PlayResultEditorCommentField(comment = playResult.comment, onCommentChange = {
                    onPlayResultChange(viewModel.editComment(playResult, it))
                })
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
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            PlayResultEditor(
                playResult = playResult,
                onPlayResultChange = onPlayResultChange,
                modifier = modifier.padding(dimensionResource(R.dimen.page_padding)),
            )
        }
    }
}

