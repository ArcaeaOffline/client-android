package xyz.sevive.arcaeaoffline.ui.common.scoreeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.activity.calculateWindowSizeClass
import xyz.sevive.arcaeaoffline.helpers.context.findActivity
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

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
    viewModel: ScoreEditorViewModel = viewModel(),
    overrideExpanded: Boolean? = null,
) {
    val context = LocalContext.current

    val score by viewModel.score.collectAsState()
    val comment by viewModel.comment.collectAsState()

    val expanded = if (overrideExpanded != null) overrideExpanded else {
        val windowSizeClass = context.findActivity()?.calculateWindowSizeClass()
        windowSizeClass != null && windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
    }

    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
        NullableNumberInput(
            value = score,
            onNumberChange = { viewModel.setScore(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.arcaea_score)) },
            maximum = 19999999,
            visualTransformation = ScoreTextVisualTransformation(),
        )

        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_page_padding))) {
                PureField(viewModel, Modifier.weight(1f), Modifier.weight(1f))
                FarField(viewModel, Modifier.weight(1f), Modifier.weight(1f))
                LostField(viewModel, Modifier.weight(1f), Modifier.weight(1f))
            }
        } else {
            PureField(viewModel, contentModifier = Modifier.weight(1f))
            FarField(viewModel, contentModifier = Modifier.weight(1f))
            LostField(viewModel, contentModifier = Modifier.weight(1f))
        }

        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_page_padding))) {
                MaxRecallField(viewModel, Modifier.weight(1f), Modifier.weight(1f))
                DateField(viewModel, Modifier.weight(1f), Modifier.weight(1f))
            }
        } else {
            MaxRecallField(viewModel, contentModifier = Modifier.weight(1f))
            DateField(viewModel, contentModifier = Modifier.weight(1f))
        }

        if (expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_page_padding))) {
                ClearTypeField(viewModel, Modifier.weight(1f), Modifier.weight(1f))
                ModifierField(viewModel, Modifier.weight(1f), Modifier.weight(1f))
            }
        } else {
            ClearTypeField(viewModel, contentModifier = Modifier.weight(1f))
            ModifierField(viewModel, contentModifier = Modifier.weight(1f))
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
                label = { Text(stringResource(R.string.score_editor_comment_field)) },
            )
        }
    }
}

@Preview
@Composable
private fun ScoreEditorPreview() {
    val scoreEditorViewModel: ScoreEditorViewModel = viewModel()
    scoreEditorViewModel.setChart("test", 2)

    ArcaeaOfflineTheme {
        Column {
            Text(scoreEditorViewModel.toArcaeaScore().toString())
            ScoreEditor(scoreEditorViewModel)
        }
    }
}
