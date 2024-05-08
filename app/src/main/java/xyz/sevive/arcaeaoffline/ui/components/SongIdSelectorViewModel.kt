package xyz.sevive.arcaeaoffline.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer


class SongIdSelectorViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    val packList = repositoryContainer.packRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    private val _chartOnly = MutableStateFlow(false)
    private val chartOnly = _chartOnly.asStateFlow()

    fun setChartOnly(value: Boolean) {
        _chartOnly.value = value
    }

    private val _songList = MutableStateFlow<List<Song>>(listOf())
    val songList = _songList.asStateFlow()

    suspend fun setSongListBySet(set: String) {
        _songList.value = repositoryContainer.songRepository.findBySet(set).map { songs ->
            if (chartOnly.value) {
                songs.filter {
                    repositoryContainer.chartRepository.findAllBySongId(it.id).first().isNotEmpty()
                }
            } else songs
        }.first()
    }

    private val _selectedPackIndex = MutableStateFlow(-1)
    val selectedPackIndex = _selectedPackIndex.asStateFlow()

    private val _selectedSongIndex = MutableStateFlow(-1)
    val selectedSongIndex = _selectedSongIndex.asStateFlow()

    fun getSelectedSongId(): String? {
        val idx = selectedSongIndex.value
        return if (idx > -1) songList.value[idx].id else null
    }

    suspend fun selectPackIndex(idx: Int) {
        _selectedPackIndex.value = idx
        _selectedSongIndex.value = -1

        setSongListBySet(packList.value[idx].id)
    }

    fun selectSongIndex(idx: Int) {
        _selectedSongIndex.value = idx
    }

    private fun reset() {
        _selectedPackIndex.value = -1
        _selectedSongIndex.value = -1
        _songList.value = listOf()
    }

    suspend fun initialSelect(songId: String?) {
        if (songId == null) {
            reset()
            return
        }

        val song = repositoryContainer.songRepository.find(songId).firstOrNull()

        if (song == null) {
            reset()
            return
        }

        val packIdx = packList.value.indexOfFirst { it.id == song.set }
        if (packIdx < 0) return
        selectPackIndex(packIdx)

        val songIdx = songList.value.indexOfFirst { it.id == song.id }
        if (songIdx < 0) return
        selectSongIndex(songIdx)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5000L
    }
}
