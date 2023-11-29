package xyz.sevive.arcaeaoffline.ui.components.scoreeditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.ui.components.CustomComboBox
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
internal fun NullableField(
    isNull: Boolean,
    onIsNullChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        content()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isNull, onCheckedChange = { onIsNullChange(it) })
            Text("NULL", softWrap = false, maxLines = 1)
        }
    }
}

class ScoreTextVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val out = text.reversed().chunked(3).joinToString("'").reversed()

        // TODO: fix this shit
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val quotes = (offset - 1) / 3
                return offset + quotes
            }

            override fun transformedToOriginal(offset: Int): Int {
                val quotes = offset / 4
                return offset - quotes
            }
        }

        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

@Composable
fun ScoreEditor(
    onScoreCommit: (Score) -> Unit,
    viewModel: ScoreEditorViewModel = viewModel(),
) {
    val score by viewModel.score.collectAsState()
    val pure by viewModel.pure.collectAsState()
    val far by viewModel.far.collectAsState()
    val lost by viewModel.lost.collectAsState()
    val maxRecall by viewModel.maxRecall.collectAsState()
    val date by viewModel.date.collectAsState()
    val clearType by viewModel.clearType.collectAsState()
    val scoreModifier by viewModel.modifier.collectAsState()
    val comment by viewModel.comment.collectAsState()

    Column {
        NullableNumberInput(
            value = score,
            onNumberChange = { viewModel.setScore(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Score") },
            maximum = 19999999,
            visualTransformation = ScoreTextVisualTransformation(),
        )

        NullableField(
            isNull = pure == null,
            onIsNullChange = { viewModel.setPureIsNull(it) },
        ) {
            NullableNumberInput(
                value = pure,
                onNumberChange = { viewModel.setPure(it) },
                label = { Text("PURE") },
                modifier = Modifier.weight(1f),
            )
        }

        NullableField(
            isNull = far == null,
            onIsNullChange = { viewModel.setFarIsNull(it) },
        ) {
            NullableNumberInput(
                value = far,
                onNumberChange = { viewModel.setFar(it) },
                label = { Text("FAR") },
                modifier = Modifier.weight(1f),
            )
        }

        NullableField(
            isNull = lost == null,
            onIsNullChange = { viewModel.setLostIsNull(it) },
        ) {
            NullableNumberInput(
                value = lost,
                onNumberChange = { viewModel.setLost(it) },
                label = { Text("LOST") },
                modifier = Modifier.weight(1f),
            )
        }

        NullableField(
            isNull = maxRecall == null,
            onIsNullChange = { viewModel.setMaxRecallIsNull(it) },
        ) {
            NullableNumberInput(
                value = maxRecall,
                onNumberChange = { viewModel.setMaxRecall(it) },
                label = { Text("MAX RECALL") },
                modifier = Modifier.weight(1f),
            )
        }

        NullableField(
            isNull = date == null,
            onIsNullChange = { viewModel.setDateIsNull(it) },
        ) {
            NullableDateTimeEditor(
                date = date,
                onDateChange = { viewModel.setDate(it) },
                modifier = Modifier.weight(1f),
            )
        }

        NullableField(
            isNull = clearType == null,
            onIsNullChange = { viewModel.setClearTypeIsNull(it) },
        ) {
            CustomComboBox(
                options = ArcaeaScoreClearType.entries.map {
                    Pair(TextFieldValue(it.name.replace("_", " ")), it.value)
                },
                selectedIndex = clearType?.value ?: -1,
                onSelectChanged = { viewModel.setClearType(it) },
                modifier = Modifier.weight(1f),
                enabled = clearType != null,
                label = { Text("Clear Type") },
            )
        }

        NullableField(
            isNull = scoreModifier == null,
            onIsNullChange = { viewModel.setModifierIsNull(it) },
        ) {
            CustomComboBox(
                options = ArcaeaScoreModifier.entries.map {
                    Pair(TextFieldValue(it.name.replace("_", " ")), it.value)
                },
                selectedIndex = scoreModifier?.value ?: -1,
                onSelectChanged = { viewModel.setModifier(it) },
                modifier = Modifier.weight(1f),
                enabled = scoreModifier != null,
                label = { Text("Modifier") },
            )
        }

        NullableField(
            isNull = comment == null,
            onIsNullChange = { viewModel.setCommentIsNull(it) },
        ) {
            TextField(
                value = comment ?: "",
                onValueChange = { viewModel.setComment(it) },
                modifier = Modifier.weight(1f),
                enabled = comment != null,
                label = { Text("Comment") },
            )
        }

        Button(onClick = { onScoreCommit(viewModel.toArcaeaScore()) }) {
            Text("Commit")
        }
    }
}

@Preview
@Composable
fun ScoreEditorPreview() {
    ArcaeaOfflineTheme {
        ScoreEditor({})
    }
}
