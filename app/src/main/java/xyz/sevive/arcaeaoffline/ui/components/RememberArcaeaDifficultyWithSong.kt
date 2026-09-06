package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository

@Composable
internal fun rememberArcaeaDifficultyWithSong(
    songId: String?,
    ratingClass: ArcaeaRatingClass?,
): State<DifficultyWithSong?> {
    val repo = koinInject<DifficultyWithSongRepository>()
    return produceState(initialValue = null, songId, ratingClass) {
        if (songId != null && ratingClass != null) {
            repo.find(songId, ratingClass).collect { value = it }
        } else {
            value = null
        }
    }
}

@Composable
internal fun rememberArcaeaChartInfo(
    songId: String?,
    ratingClass: ArcaeaRatingClass?,
): State<ChartInfo?> {
    val repo = koinInject<ChartInfoRepository>()
    return produceState(initialValue = null, songId, ratingClass) {
        if (songId != null && ratingClass != null) {
            repo.find(songId, ratingClass).collect { value = it }
        } else {
            value = null
        }
    }
}
