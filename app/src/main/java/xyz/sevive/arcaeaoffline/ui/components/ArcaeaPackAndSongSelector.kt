package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import kotlinx.coroutines.flow.firstOrNull
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository

@Composable
private fun rememberArcaeaPacks(packRepo: PackRepository): State<List<Pack>> =
    produceState(initialValue = emptyList()) {
        packRepo.findAll().collect {
            value = it
        }
    }

@Composable
private fun rememberArcaeaSongs(
    songRepo: SongRepository,
    chartInfoRepo: ChartInfoRepository,
    packId: String?,
    chartOnly: Boolean = false,
): State<List<Song>> =
    produceState(initialValue = emptyList(), packId, chartOnly) {
        if (packId != null) {
            songRepo.findBySet(packId).collect { songs ->
                value =
                    if (!chartOnly) {
                        songs
                    } else {
                        songs.filter { song ->
                            !chartInfoRepo.findAllBySongId(song.id).firstOrNull().isNullOrEmpty()
                        }
                    }
            }
        } else {
            emptyList<Song>()
        }
    }

@Composable
fun ArcaeaPackAndSongSelector(
    song: Song?,
    onSongChanged: (Song?) -> Unit,
    modifier: Modifier = Modifier,
    chartOnly: Boolean = false,
) {
    val packRepo = koinInject<PackRepository>()
    val songRepo = koinInject<SongRepository>()
    val chartInfoRepo = koinInject<ChartInfoRepository>()

    val packs by rememberArcaeaPacks(packRepo = packRepo)
    var selectedPackIndex by rememberSaveable { mutableIntStateOf(-1) }

    val songs by rememberArcaeaSongs(
        songRepo = songRepo,
        chartInfoRepo = chartInfoRepo,
        packId = packs.getOrNull(selectedPackIndex)?.id,
        chartOnly = chartOnly,
    )
    var selectedSongIndex by rememberSaveable { mutableIntStateOf(-1) }

    /**
     * `targetSongId` is the unified intent that drives the two-step async resolution:
     * 1. select the right pack
     * 2. once songs load, select the matching song.
     *
     * It is set by the quick filter, and also synced from the external `song` parameter.
     */
    var targetSongId by rememberSaveable { mutableStateOf<String?>(null) }

    // Mirror the external `song` into `targetSongId` so the initial / parent-driven
    // selection follows the same async resolution path as the quick filter.
    LaunchedEffect(song?.id) {
        // Guard against a feedback loop: when the resolution LaunchedEffect calls
        // onSongChanged, the parent updates `song`, which would otherwise re-set
        // targetSongId and trigger a redundant re-resolution.
        if (song != null && song.id != songs.getOrNull(selectedSongIndex)?.id) {
            targetSongId = song.id
        }
    }

    // Two-step async resolution: select the target's pack first, wait for
    // the pack's song list to load, then pick the song.
    LaunchedEffect(packs, songs, targetSongId) {
        val target = targetSongId ?: return@LaunchedEffect

        // packs may still be loading, wait for the next launch
        if (packs.isEmpty()) return@LaunchedEffect

        // resolve the target song to its owning pack
        val targetSong =
            songRepo.find(target).firstOrNull() ?: run {
                targetSongId = null
                return@LaunchedEffect
            }
        val packIndex = packs.indexOfFirst { it.id == targetSong.set }
        if (packIndex < 0) {
            targetSongId = null
            return@LaunchedEffect
        }

        // step 1 - ensure the correct pack is selected;
        // returning here lets recomposition update the packId passed to
        // rememberArcaeaSongs, which will re-launch this effect when songs arrive.
        if (selectedPackIndex != packIndex) {
            selectedPackIndex = packIndex
            return@LaunchedEffect
        }

        // step 2 - songs are still loading for this pack, wait for the next launch
        if (songs.isEmpty()) return@LaunchedEffect

        val idx = songs.indexOfFirst { it.id == target }
        if (idx >= 0) {
            selectedSongIndex = idx
            onSongChanged(songs[idx])
            targetSongId = null
        }
    }

    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        ArcaeaPackAndSongQuickSearch(
            onSelect = { filter ->
                // set the pack directly for instant visual feedback
                // the async resolution will set it again (to the same value) as a no-op
                selectedPackIndex = packs.indexOfFirst { it.id == filter.packId }
                targetSongId = filter.songId
            },
            modifier = Modifier.fillMaxWidth(),
            packRepo = packRepo,
            songRepo = songRepo,
        )

        ArcaeaPackSelector(
            packs = packs,
            onSelect = {
                selectedPackIndex = it
                targetSongId = null // cancel any in-flight target
            },
            selectedIndex = selectedPackIndex,
            disableIfEmpty = true,
        )

        ArcaeaSongSelector(
            songs = songs,
            onSelect = { idx ->
                songs.getOrNull(idx)?.let { song ->
                    selectedSongIndex = idx
                    targetSongId = null // cancel any in-flight target
                    onSongChanged(song)
                }
            },
            selectedIndex = selectedSongIndex,
            disableIfEmpty = true,
        )
    }
}
