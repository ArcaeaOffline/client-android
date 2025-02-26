package xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultEditorDialog
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
internal fun DatabaseAddPlayResultPlayResultAction(
    playResult: PlayResult?,
    onPlayResultChange: (PlayResult) -> Unit,
    warnings: List<ArcaeaPlayResultValidatorWarning>,
    modifier: Modifier = Modifier,
) {
    var showPlayResultEditorDialog by rememberSaveable { mutableStateOf(false) }
    if (showPlayResultEditorDialog) {
        playResult?.let {
            ArcaeaPlayResultEditorDialog(
                onDismiss = { showPlayResultEditorDialog = false },
                playResult = playResult,
                onPlayResultChange = { onPlayResultChange(it) },
            )
        }
    }

    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Box(Modifier.weight(1f)) {
            if (playResult != null) {
                Column(Modifier.fillMaxWidth()) {
                    ArcaeaPlayResultCard(
                        playResult = playResult,
                        onClick = { showPlayResultEditorDialog = true },
                        warnings = warnings,
                    )
                }
            } else {
                Card(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        IconRow(
                            Modifier
                                .minimumInteractiveComponentSize()
                                .padding(dimensionResource(R.dimen.card_padding))
                        ) {
                            Icon(Icons.Default.Block, null)
                            Text(stringResource(R.string.database_add_play_result_select_chart_first))
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = { showPlayResultEditorDialog = true },
            enabled = playResult != null,
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
        }
    }
}

@PreviewLightDark
@Composable
private fun PlayResultActionPreview() {
    val playResult = PlayResult(
        songId = "test", ratingClass = ArcaeaRatingClass.FUTURE, score = 12345678
    )

    ArcaeaOfflineTheme {
        Surface {
            Column {
                DatabaseAddPlayResultPlayResultAction(
                    playResult = null,
                    onPlayResultChange = { },
                    warnings = emptyList(),
                    modifier = Modifier.fillMaxWidth(),
                )

                DatabaseAddPlayResultPlayResultAction(
                    playResult = playResult,
                    onPlayResultChange = { },
                    warnings = emptyList(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
