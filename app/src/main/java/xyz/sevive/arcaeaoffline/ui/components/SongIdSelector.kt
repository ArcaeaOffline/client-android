package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider


@Composable
fun SongIdSelector(
    songId: String?,
    onSongIdChanged: (songId: String?) -> Unit,
    modifier: Modifier = Modifier,
    chartOnly: Boolean = false,
    viewModel: SongIdSelectorViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    val packList by viewModel.packList.collectAsState()
    val songList by viewModel.songList.collectAsState()

    val selectedPackIndex by viewModel.selectedPackIndex.collectAsState()
    val selectedSongIndex by viewModel.selectedSongIndex.collectAsState()

    LaunchedEffect(chartOnly) {
        viewModel.setChartOnly(chartOnly)
    }

    val selectPack: (Int) -> Unit = { i ->
        coroutineScope.launch {
            viewModel.selectPackIndex(i)
            onSongIdChanged(viewModel.getSelectedSongId())
        }
    }
    val selectSong: (Int) -> Unit = { i ->
        viewModel.selectSongIndex(i)
        onSongIdChanged(viewModel.getSelectedSongId())
    }

    LaunchedEffect(songId) {
        viewModel.initialSelect(songId)
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
