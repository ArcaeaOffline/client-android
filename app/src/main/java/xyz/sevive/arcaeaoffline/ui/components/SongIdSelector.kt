package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider


@Composable
fun SongIdSelector(
    onSongIdChanged: (songId: String?) -> Unit,
    modifier: Modifier = Modifier,
    chartOnly: Boolean = false,
    viewModel: SongIdSelectorViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    val packList by viewModel.packList.collectAsState()
    val basePackMap by viewModel.basePackMap.collectAsState()
    val songList by viewModel.songList.collectAsState()

    val packOptions = packList.map {
        val basePack = basePackMap[it.id]
        val name = if (basePack != null) {
            TextFieldValue(buildAnnotatedString {
                append(it.name)
                withStyle(
                    SpanStyle(fontSize = 0.75.em, color = MaterialTheme.colorScheme.secondary)
                ) {
                    append("@${basePack.name}")
                }
            })
        } else (TextFieldValue(it.name))
        Pair(name, it.id)
    }
    val songOptions = songList.map { Pair(TextFieldValue(it.title), it.id) }

    var selectedPackIndex by remember { mutableIntStateOf(-1) }
    var selectedSongIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))
    ) {
        CustomComboBox(
            options = packOptions,
            selectedIndex = selectedPackIndex,
            onSelectChanged = {
                selectedPackIndex = it
                if (it > -1) {
                    selectedSongIndex = -1
                    coroutineScope.launch { viewModel.setSongListBySet(packList[it].id, chartOnly) }
                }
            },
            label = { Text(stringResource(R.string.song_id_selector_select_pack)) },
            leadingIcon = { Icon(painterResource(R.drawable.ic_pack), null) },
            modifier = Modifier.fillMaxWidth(),
        )

        CustomComboBox(
            options = songOptions,
            selectedIndex = selectedSongIndex,
            onSelectChanged = {
                selectedSongIndex = it
                if (it > -1) onSongIdChanged(songList[it].id) else onSongIdChanged(null)
            },
            label = { Text(stringResource(R.string.song_id_selector_select_song)) },
            leadingIcon = { Icon(Icons.Default.QueueMusic, null) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
