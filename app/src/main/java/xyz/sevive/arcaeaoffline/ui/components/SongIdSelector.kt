package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val packs by viewModel.packs.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()

    val selectedPackIndex by viewModel.selectedPackIndex.collectAsStateWithLifecycle()
    val selectedSongIndex by viewModel.selectedSongIndex.collectAsStateWithLifecycle()

    val selectedSongId by viewModel.selectedSongId.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = selectedSongId) {
        if (selectedSongId != songId) onSongIdChanged(selectedSongId)
    }

    LaunchedEffect(chartOnly) {
        viewModel.setChartOnly(chartOnly)
    }

    val selectPack: (Int) -> Unit = { i ->
        viewModel.selectPackIndex(i)
    }
    val selectSong: (Int) -> Unit = { i ->
        viewModel.selectSongIndex(i)
    }

    LaunchedEffect(songId) {
        viewModel.initialSelect(songId)
    }

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))
    ) {
        ArcaeaPackSelector(
            packs = packs,
            onSelect = { selectPack(it) },
            selectedIndex = selectedPackIndex,
            disableIfEmpty = true,
        )

        ArcaeaSongSelector(
            songs = songs,
            onSelect = { selectSong(it) },
            selectedIndex = selectedSongIndex,
            disableIfEmpty = true,
        )
    }
}
