package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Song


@Composable
fun ArcaeaSongSelector(
    songs: List<Song>,
    onSelect: (index: Int) -> Unit,
    selectedIndex: Int = -1,
) {
    val labels = songs.map { buildAnnotatedString { append(it.title) } }

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
        label = { Text(stringResource(R.string.song_id_selector_select_song)) },
        leadingIcon = { Icon(Icons.AutoMirrored.Default.QueueMusic, null) },
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
        modifier = Modifier.fillMaxWidth(),
    )
}
