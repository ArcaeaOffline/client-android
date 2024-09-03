package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import java.util.UUID


// TODO: change this to kotlinx-serialization-json after ocr branch merged
private val playResultSaver: Saver<PlayResult, *> = listSaver(
    save = {
        listOf(
            it.id, it.uuid.toString(), it.songId, it.ratingClass.value,
            it.score, it.pure, it.far, it.lost, it.maxRecall,
            it.date?.toEpochMilli(), it.modifier?.value, it.clearType?.value, it.comment
        )
    },
    restore = {
        PlayResult(
            id = it[0] as Long,
            uuid = UUID.fromString(it[1] as String),
            songId = it[2] as String,
            ratingClass = ArcaeaRatingClass.fromInt(it[3] as Int),
            score = it[4] as Int,
            pure = it[5] as Int?,
            far = it[6] as Int?,
            lost = it[7] as Int?,
            maxRecall = it[8] as Int?,
            date = (it[9] as Long?)?.let { long -> Instant.ofEpochMilli(long) },
            modifier = (it[10] as Int?)?.let { int -> ArcaeaPlayResultModifier.fromInt(int) },
            clearType = (it[11] as Int?)?.let { int -> ArcaeaPlayResultClearType.fromInt(int) },
            comment = it[12] as String?
        )
    }
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
        R.string.play_result_editor_other_fields
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
//                modifier = modifier.padding(dimensionResource(R.dimen.page_padding)),
            )

        },
        confirmButton = {
            FilledIconButton(onClick = {
                onPlayResultChange(playResultCache)
                onDismiss()
            }) {
                Icon(Icons.Default.Check, contentDescription = null)
            }
        },
        dismissButton = {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
    )
}
