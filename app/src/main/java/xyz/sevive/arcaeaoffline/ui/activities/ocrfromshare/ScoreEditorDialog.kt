package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditor
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditorViewModel

@Composable
internal fun ScoreEditorDialog(
    onDismiss: () -> Unit,
    onScoreCommit: (Score) -> Unit,
    scoreEditorViewModel: ScoreEditorViewModel,
    dismissAfterScoreCommit: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface {
            LazyColumn(Modifier.padding(dimensionResource(R.dimen.general_page_padding))) {
                item {
                    ScoreEditor(
                        onScoreCommit = {
                            onScoreCommit(it)
                            if (dismissAfterScoreCommit) onDismiss()
                        },
                        viewModel = scoreEditorViewModel,
                    )
                }
            }
        }
    }
}
