package xyz.sevive.arcaeaoffline.ui.components

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class ChartSelectorViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _song = MutableStateFlow<Song?>(null)
    val song = _song.asStateFlow()

    private val _ratingClass = MutableStateFlow<ArcaeaRatingClass?>(null)
    val ratingClass = _ratingClass.asStateFlow()

    private val _enabledRatingClasses = MutableStateFlow(listOf<ArcaeaRatingClass>())
    val enabledRatingClasses = _enabledRatingClasses.asStateFlow()

    private val _ratingDetails =
        MutableStateFlow(mapOf<ArcaeaRatingClass, Pair<Int, Boolean>>())
    val ratingDetails = _ratingDetails.asStateFlow()

    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    /**
     * Update enabled rating classes with the given `songId`.
     *
     * If the `songId` is null or no difficulties found with it, set the rating classes
     * to an empty list.
     */
    private suspend fun updateRatingClassSelectorParams(song: Song?) {
        if (song == null) {
            _enabledRatingClasses.value = listOf()
            _ratingDetails.value = mapOf()
            return
        }

        val difficulties =
            repositoryContainer.difficultyRepo.findAllBySongId(song.id).firstOrNull()

        if (difficulties == null) {
            _enabledRatingClasses.value = listOf()
            _ratingDetails.value = mapOf()
            return
        }

        _enabledRatingClasses.value = difficulties.map { it.ratingClass }
        _ratingDetails.value = difficulties.associate {
            it.ratingClass to Pair(it.rating, it.ratingPlus)
        }
    }

    suspend fun setSong(song: Song?) {
        _song.value = song

        updateRatingClassSelectorParams(song)

        if (!enabledRatingClasses.value.contains(ratingClass.value) && enabledRatingClasses.value.isNotEmpty()) {
            setRatingClass(enabledRatingClasses.value.maxBy { it.value })
        }

        updateChart()
    }

    suspend fun setRatingClass(ratingClass: ArcaeaRatingClass?) {
        _ratingClass.value = ratingClass
        updateChart()
    }

    private suspend fun updateChart() {
        val song = song.value
        val ratingClass = ratingClass.value

        if (song == null || ratingClass == null) {
            _chart.value = null
            return
        }

        val chart = repositoryContainer.chartRepo.find(song.id, ratingClass).firstOrNull()
        _chart.value = chart
    }

    private fun reset() {
        _song.value = null
        _ratingClass.value = null
        _enabledRatingClasses.value = listOf()
        _ratingDetails.value = mapOf()
        _chart.value = null
    }

    suspend fun launchedEffectSet(chart: Chart?) {
        if (chart == null) {
            reset()
            return
        }

        setSong(repositoryContainer.songRepo.find(chart.songId).firstOrNull())
        setRatingClass(chart.ratingClass)
    }
}
