package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.ui.common.datetimeeditor.NullableDateTimeEditor
import kotlin.math.max


// TODO: make this work
internal class ArcaeaPlayResultScoreVisualTransformation(
    private val score: Int,
    private val fontColor: Color,
) : VisualTransformation {
    class ScoreOffsetMapping(score: Int) : OffsetMapping {
        private val size = score.toString().length

        private fun splits(): Int {
            return max(0, (size - 1) / 3)
        }

        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 10) return 10
            return offset + splits()
        }

        override fun transformedToOriginal(offset: Int): Int {
            return 0
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val scoreString = score.toString().padStart(8, '0')

        var nonZeroMet = false
        val transformedAnnotatedString = buildAnnotatedString {
            for (i in scoreString.indices) {
                val char = scoreString[i]

                if (i != 0 && (scoreString.length - i) % 3 == 0) {
                    append('\'')
                }

                if (i == scoreString.length - 1) nonZeroMet = true

                if (char == '0' && !nonZeroMet) {
                    withStyle(SpanStyle(color = fontColor.copy(alpha = 0.38f))) {
                        append(char)
                    }
                    continue
                } else nonZeroMet = true

                append(char)
            }
        }

        return TransformedText(transformedAnnotatedString, ScoreOffsetMapping(score))
    }
}

@Composable
internal fun PlayResultEditorScoreField(
    score: Int?, onScoreChange: (Int) -> Unit, modifier: Modifier = Modifier
) {
//    val fontColor = LocalContentColor.current
//    val visualTransformation = score?.let {
//        ArcaeaPlayResultScoreVisualTransformation(it, fontColor)
//    } ?: VisualTransformation { TransformedText(it, OffsetMapping.Identity) }

    NullableNumberInput(
        value = score,
        onValueChange = { onScoreChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_score)) },
        maximum = Int.MAX_VALUE,
        modifier = modifier,
    )
}

@Composable
internal fun <T : Any> rememberLastNotNullValue(value: T?, defaultValueConstructor: () -> T): T {
    var lastNotNullValue by rememberSaveable { mutableStateOf(defaultValueConstructor()) }

    LaunchedEffect(value) {
        value?.let { lastNotNullValue = it }
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
        Text(stringResource(R.string.play_result_editor_set_null))
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
            onValueChange = onValueChange,
            label = label,
            minimum = 0,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PlayResultEditorPureField(
    pure: Int?, onPureChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = pure,
        onValueChange = { onPureChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_pure)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorFarField(
    far: Int?, onFarChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = far,
        onValueChange = { onFarChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_far)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorLostField(
    lost: Int?, onLostChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = lost,
        onValueChange = { onLostChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_lost)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorMaxRecallField(
    maxRecall: Int?, onMaxRecallChange: (Int?) -> Unit, modifier: Modifier = Modifier
) {
    NullableNumberInputWithCheckboxWrapper(
        value = maxRecall,
        onValueChange = { onMaxRecallChange(it) },
        label = { Text(stringResource(R.string.arcaea_play_result_max_recall)) },
        modifier = modifier,
    )
}

@Composable
internal fun PlayResultEditorDateTimeField(
    instant: Instant?, onInstantChange: (Instant?) -> Unit, modifier: Modifier = Modifier
) {
    GeneralNullableFieldWrapper(
        value = instant,
        defaultValueConstructor = { Instant.now() },
        onValueChange = onInstantChange,
        modifier = modifier,
    ) {
        NullableDateTimeEditor(
            date = if (instant != null) LocalDateTime.ofInstant(
                instant, ZoneId.systemDefault()
            ) else null,
            onDateChange = {
                onInstantChange(it.toInstant(ZoneId.systemDefault().rules.getOffset(it)))
            },
            Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PlayResultEditorClearTypeField(
    clearType: ArcaeaPlayResultClearType?,
    onClearTypeChange: (ArcaeaPlayResultClearType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val values = remember { ArcaeaPlayResultClearType.entries.sortedBy { it.value } }
    val labels = remember { values.map { AnnotatedString(it.toDisplayString()) } }
    val textFieldValue = remember(clearType) { clearType?.toDisplayString() ?: "" }

    var showSelectDialog by rememberSaveable { mutableStateOf(false) }
    if (showSelectDialog) {
        SelectDialog(
            labels = labels,
            onDismiss = { showSelectDialog = false },
            onSelect = { onClearTypeChange(values[it]) },
            selectedOptionIndex = clearType?.value,
        )
    }

    val editable = remember(clearType) { clearType != null }
    val tryShowSelectDialog = remember(editable) {
        { if (editable) showSelectDialog = true }
    }

    GeneralNullableFieldWrapper(
        value = clearType,
        defaultValueConstructor = { ArcaeaPlayResultClearType.NORMAL_CLEAR },
        onValueChange = onClearTypeChange,
        modifier = modifier,
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = {},
            modifier = Modifier
                .clickable(editable) { tryShowSelectDialog() }
                .weight(1f),
            readOnly = true,
            enabled = editable,
            label = { Text(stringResource(R.string.arcaea_play_result_clear_type)) },
            trailingIcon = {
                IconButton(onClick = { tryShowSelectDialog() }, enabled = editable) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            },
        )
    }
}

@Composable
internal fun PlayResultEditorModifierField(
    arcaeaModifier: ArcaeaPlayResultModifier?,
    onArcaeaModifierChange: (ArcaeaPlayResultModifier?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val values = remember { ArcaeaPlayResultModifier.entries.sortedBy { it.value } }
    val labels = remember { values.map { AnnotatedString(it.toDisplayString()) } }
    val textFieldValue = remember(arcaeaModifier) { arcaeaModifier?.toDisplayString() ?: "" }

    var showSelectDialog by rememberSaveable { mutableStateOf(false) }
    if (showSelectDialog) {
        SelectDialog(
            labels = labels,
            onDismiss = { showSelectDialog = false },
            onSelect = { onArcaeaModifierChange(values[it]) },
            selectedOptionIndex = arcaeaModifier?.value,
        )
    }

    val editable = remember(arcaeaModifier) { arcaeaModifier != null }
    val tryShowSelectDialog = remember(editable) {
        { if (editable) showSelectDialog = true }
    }

    GeneralNullableFieldWrapper(
        value = arcaeaModifier,
        defaultValueConstructor = { ArcaeaPlayResultModifier.NORMAL },
        onValueChange = onArcaeaModifierChange,
        modifier = modifier,
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = {},
            modifier = Modifier
                .clickable(editable) { tryShowSelectDialog() }
                .weight(1f),
            readOnly = true,
            enabled = editable,
            label = { Text(stringResource(R.string.arcaea_play_result_modifier)) },
            trailingIcon = {
                IconButton(onClick = { tryShowSelectDialog() }, enabled = editable) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            },
        )
    }
}

@Composable
internal fun PlayResultEditorCommentField(
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
            label = { Text(stringResource(R.string.play_result_editor_comment_field)) },
        )
    }
}
