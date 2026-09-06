package xyz.sevive.arcaeaoffline.ui.screens.database.b30list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultBestRepository
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.helpers.UiDisplayChartCacheHolder
import kotlin.time.Duration.Companion.seconds

class DatabaseB30ListViewModel(
    playResultBestRepo: PlayResultBestRepository,
    difficultyWithSongRepo: DifficultyWithSongRepository,
    chartInfoRepo: ChartInfoRepository,
) : ViewModel() {
    data class ListItem(
        val index: Int,
        val playResultBest: PlayResultCalculated,
        val difficultyWithSong: DifficultyWithSong?,
        val chartInfo: ChartInfo?,
        val playRatingText: String = ArcaeaFormatters.potentialToText(playResultBest.playRating),
    )

    data class UiState(
        val isLoading: Boolean = false,
        val limit: Int = 0,
        val listItems: List<ListItem> = emptyList(),
    )

    private val limit = MutableStateFlow(INIT_LIMIT)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState =
        limit
            .transformLatest { limit ->
                emit(UiState(isLoading = true, limit = limit))

                playResultBestRepo
                    .orderDescWithLimit(limit)
                    .collectLatest { dbItems ->
                        val chartCacheHolder = UiDisplayChartCacheHolder()
                        chartCacheHolder.updateCache(
                            dbItems.map { it.playResult.songId to it.playResult.ratingClass },
                            difficultyWithSongRepo,
                            chartInfoRepo,
                        )

                        val listItems =
                            dbItems.mapIndexed { i, dbItem ->
                                val display = chartCacheHolder.get(dbItem.playResult)

                                ListItem(
                                    index = i,
                                    playResultBest = dbItem,
                                    difficultyWithSong = display?.difficultyWithSong,
                                    chartInfo = display?.chartInfo,
                                )
                            }

                        emit(UiState(isLoading = false, limit = limit, listItems = listItems))
                    }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
                UiState(),
            )

    fun setLimit(limit: Int) {
        this.limit.value = limit
    }

    fun forceReload() {
        val limitValue = this.limit.value
        this.limit.value = 0
        this.limit.value = limitValue
    }

    companion object {
        const val INIT_LIMIT = 40
    }
}
