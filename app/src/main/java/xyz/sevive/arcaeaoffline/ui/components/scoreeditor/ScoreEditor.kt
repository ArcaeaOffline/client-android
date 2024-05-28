package xyz.sevive.arcaeaoffline.ui.components.scoreeditor

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
import xyz.sevive.arcaeaoffline.core.database.entities.Score

@Composable
fun ScoreEditor(
    score: Score,
    onScoreChange: (Score) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScoreEditorViewModel = viewModel()
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(R.string.score_editor_main_fields, R.string.score_editor_other_fields)

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
                ScoreEditorScoreField(score = score.score, onScoreChange = {
                    onScoreChange(viewModel.editScore(score, it))
                }, modifier = Modifier.fillMaxWidth())

                ScoreEditorPureField(pure = score.pure, onPureChange = {
                    onScoreChange(viewModel.editPure(score, it))
                })
                ScoreEditorFarField(far = score.far, onFarChange = {
                    onScoreChange(viewModel.editFar(score, it))
                })
                ScoreEditorLostField(lost = score.lost, onLostChange = {
                    onScoreChange(viewModel.editLost(score, it))
                })

                ScoreEditorDateTimeField(
                    instant = score.date,
                    onInstantChange = {
                        onScoreChange(viewModel.editDate(score, it))
                    },
                )

                ScoreEditorMaxRecallField(maxRecall = score.maxRecall, onMaxRecallChange = {
                    onScoreChange(viewModel.editMaxRecall(score, it))
                })
            }

            1 -> {
                ScoreEditorModifierField(scoreModifier = score.modifier, onScoreModifierChange = {
                    onScoreChange(viewModel.editModifier(score, it))
                })

                ScoreEditorClearTypeField(clearType = score.clearType, onClearTypeChange = {
                    onScoreChange(viewModel.editClearType(score, it))
                })

                ScoreEditorCommentField(comment = score.comment, onCommentChange = {
                    onScoreChange(viewModel.editComment(score, it))
                })
            }
        }
    }
}

@Composable
fun ScoreEditorDialog(
    onDismiss: () -> Unit,
    score: Score,
    onScoreChange: (Score) -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            ScoreEditor(
                score = score,
                onScoreChange = onScoreChange,
                modifier = modifier.padding(dimensionResource(R.dimen.page_padding)),
            )
        }
    }
}

