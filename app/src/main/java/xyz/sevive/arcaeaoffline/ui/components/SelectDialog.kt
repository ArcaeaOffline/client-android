package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import xyz.sevive.arcaeaoffline.R

/**
 * A simple selection dialog.
 *
 * @param labels A list of annotated string that is passed to `Text(label)`.
 * @param onDismiss `Dialog.onDismissRequest`.
 * @param onSelect Callback function when an option is selected.
 * @param selectedOptionIndex Only for `RadioButton` rendering.
 * If the value is a negative number or `null`, none of the `RadioButton` will be checked.
 */
@Composable
fun SelectDialog(
    labels: List<AnnotatedString>,
    onDismiss: () -> Unit,
    onSelect: (index: Int) -> Unit,
    selectedOptionIndex: Int?,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth()) {
            if (labels.isEmpty()) {
                Box(Modifier.padding(dimensionResource(R.dimen.page_padding) * 2)) {
                    Text("No options available.")
                }
            } else {
                LazyColumn(Modifier.fillMaxWidth()) {
                    labels.forEachIndexed { i, label ->
                        val optionSelected = i == selectedOptionIndex

                        item {
                            Row(
                                Modifier
                                    .clickable { onSelect(i) }
                                    .background(
                                        if (optionSelected) MaterialTheme.colorScheme.primary.copy(
                                            0.1f
                                        ) else Color.Unspecified
                                    )
                                    .padding(16.dp, 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding) * 2),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = optionSelected,
                                    onClick = null,
                                )

                                Text(label)
                            }
                        }
                    }
                }
            }
        }
    }
}
