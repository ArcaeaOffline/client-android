package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository

@Composable
private fun rememberArcaeaSong(
    songRepo: SongRepository,
    songId: String?,
): State<Song?> =
    produceState(initialValue = null, songId) {
        songId?.let {
            songRepo.find(songId).collect { value = it }
        }
    }

@Composable
private fun rememberArcaeaDifficulties(
    difficultyRepo: DifficultyRepository,
    song: Song?,
): State<List<Difficulty>> =
    produceState(initialValue = emptyList(), song?.id) {
        song?.let {
            difficultyRepo.findAllBySongId(it.id).collect { value = it }
        }
    }

@Composable
fun ArcaeaChartSelector(
    songId: String?,
    ratingClass: ArcaeaRatingClass?,
    onDifficultyChange: (Difficulty) -> Unit,
) {
    val songRepo = koinInject<SongRepository>()
    val difficultyRepo = koinInject<DifficultyRepository>()

    var selectedSongId by rememberSaveable(songId) { mutableStateOf(songId) }
    var selectedRatingClass by rememberSaveable(ratingClass) {
        mutableStateOf(ratingClass)
    }
    val song by rememberArcaeaSong(songRepo = songRepo, songId = selectedSongId)
    val difficulties by rememberArcaeaDifficulties(difficultyRepo = difficultyRepo, song = song)

    val selectorItems =
        remember(difficulties) {
            difficulties.toRatingClassSelectorItems()
        }

    // Emit the change when either [selectedSongId] or [selectedRatingClass] changes.
    // Since [selectedSongId] will eventually cause [difficulties] to change, and
    // considering the produceState delay, we use [difficulties] as key instead.
    LaunchedEffect(difficulties, selectedRatingClass) {
        selectedRatingClass?.let { target ->
            difficulties.find { it.ratingClass == target }?.let(onDifficultyChange)
        }
    }

    Column(
        Modifier.padding(dimensionResource(R.dimen.card_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        ArcaeaPackAndSongSelector(
            song = song,
            onSongChanged = { selectedSongId = it?.id },
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding)),
        ) {
            Icon(painterResource(R.drawable.ic_rating_class), contentDescription = null)

            ArcaeaRatingClassSelector(
                items = selectorItems,
                selectedRatingClass = selectedRatingClass,
                onRatingClassChange = { selectedRatingClass = it },
            )
        }
    }
}
