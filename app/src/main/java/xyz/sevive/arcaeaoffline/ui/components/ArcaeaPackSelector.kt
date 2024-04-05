package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Pack


@Composable
fun ArcaeaPackSelector(
    packs: List<Pack>,
    onSelect: (index: Int) -> Unit,
    selectedIndex: Int = -1,
) {
    val basePackMap: Map<String, Pack> = packs.filter { !it.isAppendPack() }
        .associateBy(keySelector = { it.id }, valueTransform = { it })

    val labels = packs.map {
        val basePack = basePackMap[it.basePackId()]
        val packLabel = if (basePack != null) {
            buildAnnotatedString {
                append(it.name)
                withStyle(
                    SpanStyle(fontSize = 0.8.em, color = MaterialTheme.colorScheme.secondary)
                ) {
                    append("@${basePack.name}")
                }
            }
        } else buildAnnotatedString { append(it.name) }

        return@map packLabel
    }

    var showSelectDialog by rememberSaveable { mutableStateOf(false) }
    if (showSelectDialog) {
        SelectDialog(
            labels = labels,
            onDismiss = { showSelectDialog = false },
            onSelect = {
                onSelect(it)
                showSelectDialog = false
            },
            selectedOptionIndex = selectedIndex,
        )
    }

    ReadonlyClickableTextField(
        value = if (selectedIndex > -1) TextFieldValue(labels[selectedIndex]) else null,
        onClick = { showSelectDialog = true },
        label = { Text(stringResource(R.string.song_id_selector_select_pack)) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_pack), null) },
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
        modifier = Modifier.fillMaxWidth(),
    )
}
