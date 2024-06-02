package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val tabs = listOf(
        R.string.play_result_editor_main_fields,
        R.string.play_result_editor_other_fields
    )

    val playResultInVM by viewModel.playResult.collectAsStateWithLifecycle()
    LaunchedEffect(playResult) {
        if (playResult != playResultInVM) viewModel.setPlayResult(playResult)
    }
    LaunchedEffect(playResultInVM) {
        if (playResultInVM != null && playResult != playResultInVM) {
            onPlayResultChange(playResultInVM!!)
        }
    }

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = tabIndex) {
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
                    onScoreChange = { viewModel.editScore(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                PlayResultEditorPureField(
                    pure = playResult.pure,
                    onPureChange = { viewModel.editPure(it) },
                )
                PlayResultEditorFarField(
                    far = playResult.far,
                    onFarChange = { viewModel.editFar(it) },
                )
                PlayResultEditorLostField(
                    lost = playResult.lost,
                    onLostChange = { viewModel.editLost(it) },
                )

                PlayResultEditorDateTimeField(
                    instant = playResult.date,
                    onInstantChange = { viewModel.editDate(it) },
                )

                PlayResultEditorMaxRecallField(
                    maxRecall = playResult.maxRecall,
                    onMaxRecallChange = { viewModel.editMaxRecall(it) },
                )
            }

            1 -> {
                PlayResultEditorModifierField(
                    arcaeaModifier = playResult.modifier,
                    onArcaeaModifierChange = { viewModel.editModifier(it) },
                )

                PlayResultEditorClearTypeField(
                    clearType = playResult.clearType,
                    onClearTypeChange = { viewModel.editClearType(it) },
                )

                PlayResultEditorCommentField(
                    comment = playResult.comment,
                    onCommentChange = { viewModel.editComment(it) },
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

