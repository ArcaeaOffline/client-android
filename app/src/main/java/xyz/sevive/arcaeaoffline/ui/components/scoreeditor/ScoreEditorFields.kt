package xyz.sevive.arcaeaoffline.ui.components.scoreeditor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.ui.common.datetimeeditor.NullableDateTimeEditor
import xyz.sevive.arcaeaoffline.ui.components.CustomComboBox

@Composable
fun ScoreEditorScoreField(
    score: Int?, onScoreChange: (Int) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInput(
        value = score,
        onNumberChange = { onScoreChange(it) },
        label = { Text(stringResource(R.string.arcaea_score)) },
        maximum = 19999999,
        modifier = modifier,
//            visualTransformation = null,
    )
}

@Composable
internal fun <T : Any> rememberLastNotNullValue(value: T?, defaultValueConstructor: () -> T): T {
    var lastNotNullValue by rememberSaveable { mutableStateOf(defaultValueConstructor()) }

    LaunchedEffect(value) {
        if (value != null) {
            lastNotNullValue = value
        }
    }

    return lastNotNullValue
}

@Composable
internal fun SetNullCheckbox(
    isNull: Boolean,
    onIsNullChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isNull,
            onCheckedChange = onIsNullChange,
        )
        Text(stringResource(R.string.score_editor_set_null))
    }
}

@Composable
internal fun <T : Any> GeneralNullableFieldWrapper(
    value: T?,
    defaultValueConstructor: () -> T,
    onValueChange: (T?) -> Unit,
    modifier: Modifier = Modifier,
    valueComponent: @Composable RowScope.() -> Unit,
) {
    val lastNotNullValue = rememberLastNotNullValue(
        value = value,
        defaultValueConstructor = defaultValueConstructor,
    )

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        valueComponent()

        SetNullCheckbox(isNull = value == null, onIsNullChange = {
            onValueChange(if (it) null else lastNotNullValue)
        })
    }
}

@Composable
internal fun NullableNumberInputWithCheckboxWrapper(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    GeneralNullableFieldWrapper(
        value = value,
        defaultValueConstructor = { 0 },
        onValueChange = onValueChange,
        modifier = modifier,
    ) {
        NullableNumberInput(
            value = value,
            onNumberChange = onValueChange,
            label = label,
            minimum = 0,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ScoreEditorPureField(
    pure: Int?, onPureChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = pure,
        onValueChange = { onPureChange(it) },
        label = { Text(stringResource(R.string.arcaea_pure)) },
        modifier = modifier,
    )
}

@Composable
fun ScoreEditorFarField(
    far: Int?, onFarChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = far,
        onValueChange = { onFarChange(it) },
        label = { Text(stringResource(R.string.arcaea_far)) },
        modifier = modifier,
    )
}

@Composable
fun ScoreEditorLostField(
    lost: Int?, onLostChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = lost,
        onValueChange = { onLostChange(it) },
        label = { Text(stringResource(R.string.arcaea_lost)) },
        modifier = modifier,
    )
}

@Composable
fun ScoreEditorMaxRecallField(
    maxRecall: Int?, onMaxRecallChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = maxRecall,
        onValueChange = { onMaxRecallChange(it) },
        label = { Text(stringResource(R.string.arcaea_max_recall)) },
        modifier = modifier,
    )
}

@Composable
fun ScoreEditorDateTimeField(
    instant: Instant?,
    onInstantChange: (Instant?) -> Unit,
    modifier: Modifier = Modifier
) {
    GeneralNullableFieldWrapper(
        value = instant,
        defaultValueConstructor = { Instant.now() },
        onValueChange = onInstantChange,
        modifier = modifier,
    ) {
        NullableDateTimeEditor(
            date = if (instant != null) LocalDateTime.ofInstant(
                instant,
                ZoneId.systemDefault()
            ) else null,
            onDateChange = {
                onInstantChange(it.toInstant(ZoneId.systemDefault().rules.getOffset(it)))
            },
            Modifier.weight(1f),
        )
    }
}

@Composable
fun ScoreEditorClearTypeField(
    clearType: ArcaeaPlayResultClearType?,
    onClearTypeChange: (ArcaeaPlayResultClearType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    GeneralNullableFieldWrapper(
        value = clearType,
        defaultValueConstructor = { ArcaeaPlayResultClearType.NORMAL_CLEAR },
        onValueChange = onClearTypeChange,
        modifier = modifier,
    ) {
        CustomComboBox(
            options = ArcaeaPlayResultClearType.entries.map {
                Pair(TextFieldValue(it.toDisplayString()), it.value)
            },
            selectedIndex = clearType?.value ?: -1,
            onSelectChanged = { onClearTypeChange(ArcaeaPlayResultClearType.fromInt(it)) },
            enabled = clearType != null,
            label = { Text(stringResource(R.string.arcaea_score_clear_type)) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ScoreEditorModifierField(
    scoreModifier: ArcaeaPlayResultModifier?,
    onScoreModifierChange: (ArcaeaPlayResultModifier?) -> Unit,
    modifier: Modifier = Modifier,
) {
    GeneralNullableFieldWrapper(
        value = scoreModifier,
        defaultValueConstructor = { ArcaeaPlayResultModifier.NORMAL },
        onValueChange = onScoreModifierChange,
        modifier = modifier,
    ) {
        CustomComboBox(
            options = ArcaeaPlayResultModifier.entries.map {
                Pair(TextFieldValue(it.toDisplayString()), it.value)
            },
            selectedIndex = scoreModifier?.value ?: -1,
            onSelectChanged = { onScoreModifierChange(ArcaeaPlayResultModifier.fromInt(it)) },
            enabled = scoreModifier != null,
            label = { Text(stringResource(R.string.arcaea_score_modifier)) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ScoreEditorCommentField(
    comment: String?,
    onCommentChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    GeneralNullableFieldWrapper(
        value = comment,
        defaultValueConstructor = { "" },
        onValueChange = onCommentChange,
        modifier = modifier,
    ) {
        TextField(
            value = comment ?: "",
            onValueChange = { onCommentChange(it) },
            modifier = Modifier.weight(1f),
            enabled = comment != null,
            label = { Text(stringResource(R.string.score_editor_comment_field)) },
        )
    }
}

