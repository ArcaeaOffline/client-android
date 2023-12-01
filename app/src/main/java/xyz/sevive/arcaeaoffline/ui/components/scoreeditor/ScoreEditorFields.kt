package xyz.sevive.arcaeaoffline.ui.components.scoreeditor

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.ui.components.CustomComboBox

// TODO: a more elegant way...?
@Composable
internal fun NullableField(
    isNull: Boolean,
    onIsNullChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        content()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isNull, onCheckedChange = { onIsNullChange(it) })
            Text(stringResource(R.string.score_editor_set_null), softWrap = false, maxLines = 1)
        }
    }
}

@Composable
internal fun NullableNumberField(
    number: Int?,
    onIsNullChange: (Boolean) -> Unit,
    onNumberChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    minimum: Int = 0,
    maximum: Int = Int.MAX_VALUE,
) {
    NullableField(isNull = number == null, onIsNullChange = onIsNullChange, modifier = modifier) {
        NullableNumberInput(
            value = number,
            onNumberChange = onNumberChange,
            label = label,
            modifier = contentModifier,
            minimum = minimum,
            maximum = maximum,
        )
    }
}

@Composable
internal fun PureField(
    viewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val pure by viewModel.pure.collectAsState()

    NullableNumberField(
        number = pure,
        onIsNullChange = { viewModel.setPureIsNull(it) },
        onNumberChange = { viewModel.setPure(it) },
        modifier = modifier,
        contentModifier = contentModifier,
        label = { Text(stringResource(R.string.arcaea_pure)) },
    )
}

@Composable
internal fun FarField(
    viewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val far by viewModel.far.collectAsState()

    NullableNumberField(
        number = far,
        onIsNullChange = { viewModel.setFarIsNull(it) },
        onNumberChange = { viewModel.setFar(it) },
        modifier = modifier,
        contentModifier = contentModifier,
        label = { Text(stringResource(R.string.arcaea_far)) },
    )
}

@Composable
internal fun LostField(
    viewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val lost by viewModel.lost.collectAsState()

    NullableNumberField(
        number = lost,
        onIsNullChange = { viewModel.setLostIsNull(it) },
        onNumberChange = { viewModel.setLost(it) },
        modifier = modifier,
        contentModifier = contentModifier,
        label = { Text(stringResource(R.string.arcaea_lost)) },
    )
}

@Composable
internal fun MaxRecallField(
    viewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val maxRecall by viewModel.maxRecall.collectAsState()

    NullableNumberField(
        number = maxRecall,
        onIsNullChange = { viewModel.setMaxRecallIsNull(it) },
        onNumberChange = { viewModel.setMaxRecall(it) },
        modifier = modifier,
        contentModifier = contentModifier,
        label = { Text("MAX RECALL") },
    )
}

@Composable
internal fun DateField(
    viewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val date by viewModel.date.collectAsState()

    NullableField(
        isNull = date == null,
        onIsNullChange = { viewModel.setDateIsNull(it) },
        modifier = modifier,
    ) {
        NullableDateTimeEditor(
            date = date,
            onDateChange = { viewModel.setDate(it) },
            modifier = contentModifier,
        )
    }
}

@Composable
internal fun ClearTypeField(
    viewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val clearType by viewModel.clearType.collectAsState()

    NullableField(
        isNull = clearType == null,
        onIsNullChange = { viewModel.setClearTypeIsNull(it) },
        modifier = modifier,
    ) {
        CustomComboBox(
            options = ArcaeaScoreClearType.entries.map {
                Pair(TextFieldValue(it.toDisplayString()), it.value)
            },
            selectedIndex = clearType?.value ?: -1,
            onSelectChanged = { viewModel.setClearType(it) },
            modifier = contentModifier,
            enabled = clearType != null,
            label = { Text("Clear Type") },
        )
    }
}

@Composable
internal fun ModifierField(
    viewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
) {
    val scoreModifier by viewModel.modifier.collectAsState()

    NullableField(
        isNull = scoreModifier == null,
        onIsNullChange = { viewModel.setModifierIsNull(it) },
        modifier = modifier,
    ) {
        CustomComboBox(
            options = ArcaeaScoreModifier.entries.map {
                Pair(TextFieldValue(it.toDisplayString()), it.value)
            },
            selectedIndex = scoreModifier?.value ?: -1,
            onSelectChanged = { viewModel.setModifier(it) },
            modifier = contentModifier,
            enabled = scoreModifier != null,
            label = { Text("Modifier") },
        )
    }
}
