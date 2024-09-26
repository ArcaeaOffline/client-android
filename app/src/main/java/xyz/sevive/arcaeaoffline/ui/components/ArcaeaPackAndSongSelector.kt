package xyz.sevive.arcaeaoffline.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl


private const val LOG_TAG = "PackAndSongSelector"

@Composable
private fun rememberArcaeaPacks(packRepo: PackRepository): State<List<Pack>> {
    return produceState(initialValue = emptyList()) {
        packRepo.findAll().collect {
            value = it
        }
    }
}

@Composable
private fun rememberArcaeaSongs(
    songRepo: SongRepository,
    chartInfoRepo: ChartInfoRepository,
    packId: String?,
    chartOnly: Boolean = false,
): State<List<Song>> {
    return produceState(initialValue = emptyList(), packId, chartOnly) {
        if (packId != null) {
            songRepo.findBySet(packId).collect { songs ->
                value = if (!chartOnly) songs else songs.filter { song ->
                    !chartInfoRepo.findAllBySongId(song.id).firstOrNull().isNullOrEmpty()
                }
            }
        } else emptyList<Song>()
    }
}

@Composable
fun ArcaeaPackAndSongSelector(
    song: Song?,
    onSongChanged: (Song?) -> Unit,
    modifier: Modifier = Modifier,
    chartOnly: Boolean = false,
) {
    val context = LocalContext.current
    val repositoryContainer = remember {
        ArcaeaOfflineDatabaseRepositoryContainerImpl(context)
    }

    val packs by rememberArcaeaPacks(packRepo = repositoryContainer.packRepo)
    var selectedPackIndex by rememberSaveable { mutableIntStateOf(-1) }

    val songs by rememberArcaeaSongs(
        songRepo = repositoryContainer.songRepo,
        chartInfoRepo = repositoryContainer.chartInfoRepo,
        packId = packs.getOrNull(selectedPackIndex)?.id,
        chartOnly = chartOnly,
    )
    var selectedSongIndex by rememberSaveable { mutableIntStateOf(-1) }

    LaunchedEffect(song, packs, songs) {
        if (song == null || packs.isEmpty()) return@LaunchedEffect
        Log.v(LOG_TAG, "LaunchedEffect launched with song ${song.id}")

        if (selectedPackIndex == -1) {
            // song is a directly passed in parameter, so pack isn't selected
            // select it to trigger a recompose
            val packIndex = packs.indexOfFirst { it.id == song.set }
            if (packIndex == -1) {
                Log.v(LOG_TAG, "LaunchedEffect rejected because pack not found")
                return@LaunchedEffect
            }
            selectedPackIndex = packIndex
        }

        if (songs.isEmpty()) {
            Log.v(LOG_TAG, "LaunchedEffect rejected because songs empty")
            return@LaunchedEffect
        }

        selectedSongIndex = songs.indexOfFirst { it.id == song.id }
    }

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))
    ) {
        ArcaeaPackSelector(
            packs = packs,
            onSelect = { selectedPackIndex = it },
            selectedIndex = selectedPackIndex,
            disableIfEmpty = true,
        )

        ArcaeaSongSelector(
            songs = songs,
            onSelect = { idx ->
                songs.getOrNull(idx)?.let { song ->
                    selectedSongIndex = idx
                    onSongChanged(song)
                }
            },
            selectedIndex = selectedSongIndex,
            disableIfEmpty = true,
        )
    }
}
