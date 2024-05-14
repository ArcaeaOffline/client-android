package xyz.sevive.arcaeaoffline.ui.components

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class ChartSelectorViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _songId = MutableStateFlow<String?>(null)
    val songId = _songId.asStateFlow()

    private val _ratingClass = MutableStateFlow<ArcaeaScoreRatingClass?>(null)
    val ratingClass = _ratingClass.asStateFlow()

    private val _enabledRatingClasses = MutableStateFlow(listOf<ArcaeaScoreRatingClass>())
    val enabledRatingClasses = _enabledRatingClasses.asStateFlow()

    private val _ratingDetails =
        MutableStateFlow(mapOf<ArcaeaScoreRatingClass, Pair<Int, Boolean>>())
    val ratingDetails = _ratingDetails.asStateFlow()

    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    /**
     * Update enabled rating classes with the given `songId`.
     *
     * If the `songId` is null or no difficulties found with it, set the rating classes
     * to an empty list.
     */
    private suspend fun updateRatingClassSelectorParams(songId: String?) {
        if (songId == null) {
            _enabledRatingClasses.value = listOf()
            _ratingDetails.value = mapOf()
            return
        }

        val difficulties =
            repositoryContainer.difficultyRepository.findAllBySongId(songId).firstOrNull()

        if (difficulties == null) {
            _enabledRatingClasses.value = listOf()
            _ratingDetails.value = mapOf()
            return
        }

        _enabledRatingClasses.value = difficulties.map {
            ArcaeaScoreRatingClass.fromInt(it.ratingClass)
        }
        _ratingDetails.value = difficulties.associate {
            ArcaeaScoreRatingClass.fromInt(it.ratingClass) to Pair(it.rating, it.ratingPlus)
        }
    }

    suspend fun setSongId(songId: String?) {
        _songId.value = songId

        updateRatingClassSelectorParams(songId)

        if (
            !enabledRatingClasses.value.contains(ratingClass.value)
            && enabledRatingClasses.value.isNotEmpty()
        ) {
            setRatingClass(enabledRatingClasses.value.last())
        }

        updateChart()
    }

    suspend fun setRatingClass(ratingClass: ArcaeaScoreRatingClass?) {
        _ratingClass.value = ratingClass
        updateChart()
    }

    private suspend fun updateChart() {
        val songId = songId.value
        val ratingClass = ratingClass.value?.value

        if (songId == null || ratingClass == null) {
            _chart.value = null
            return
        }

        val chart = ChartFactory.getChart(repositoryContainer, songId, ratingClass)
        _chart.value = chart
    }

    private fun reset() {
        _songId.value = null
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

        setSongId(chart.songId)
        setRatingClass(ArcaeaScoreRatingClass.fromInt(chart.ratingClass))
    }
}
