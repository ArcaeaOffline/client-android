package xyz.sevive.arcaeaoffline.ui.common.scoreeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.ui.components.IconRow

@Composable
fun ScoreEditorDialog(
    onDismiss: () -> Unit,
    onScoreCommit: (Score) -> Unit,
    scoreEditorViewModel: ScoreEditorViewModel,
    dismissAfterScoreCommit: Boolean = true,
) {
    val scoreId by scoreEditorViewModel.scoreId.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Scaffold(
            topBar = {
                val text = if (scoreId != null && scoreId!! > 0) {
                    "Editing score ID $scoreId"
                } else {
                    "Editing score"
                }

                Text(
                    text,
                    Modifier.padding(dimensionResource(R.dimen.general_page_padding)),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            bottomBar = {
                HorizontalDivider()

                Row(
                    Modifier.padding(dimensionResource(R.dimen.general_page_padding)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
                ) {
                    Spacer(Modifier.weight(1f))

                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                    ) {
                        IconRow(icon = { Icon(Icons.Default.Close, null) }) {
                            Text(stringResource(R.string.general_cancel))
                        }
                    }

                    Button(onClick = {
                        onScoreCommit(scoreEditorViewModel.toArcaeaScore())
                        if (dismissAfterScoreCommit) onDismiss()
                    }) {
                        IconRow(icon = { Icon(Icons.Default.Check, null) }) {
                            Text(stringResource(R.string.general_ok))
                        }
                    }
                }
            },
        ) {
            LazyColumn(
                Modifier
                    .padding(it)
                    .padding(dimensionResource(R.dimen.general_page_padding))
            ) {
                item {
                    ScoreEditor(viewModel = scoreEditorViewModel)
                }
            }
        }
    }
}
