package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
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
    val songList by viewModel.songList.collectAsState()

    var selectedPackIndex by remember { mutableIntStateOf(-1) }
    var selectedSongIndex by remember { mutableIntStateOf(-1) }

    val selectPack: (Int) -> Unit = { i ->
        selectedPackIndex = i
        selectedSongIndex = -1
        coroutineScope.launch {
            viewModel.setSongListBySet(packList[selectedPackIndex].id, chartOnly)
        }
    }
    val selectSong: (Int) -> Unit = { i ->
        selectedSongIndex = i
        onSongIdChanged(songList[i].id)
    }

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))
    ) {
        ArcaeaPackSelector(
            packs = packList,
            onSelect = { selectPack(it) },
            selectedIndex = selectedPackIndex,
        )

        ArcaeaSongSelector(
            songs = songList,
            onSelect = { selectSong(it) },
            selectedIndex = selectedSongIndex,
        )
    }
}
