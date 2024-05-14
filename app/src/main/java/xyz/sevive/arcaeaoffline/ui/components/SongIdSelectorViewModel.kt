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
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer


class SongIdSelectorViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _packs = MutableStateFlow<List<Pack>>(listOf())
    val packs = _packs.asStateFlow()

    private val packSongsMap: MutableMap<String, List<Song>> = mutableMapOf()

    private val _songs = MutableStateFlow<List<Song>>(listOf())
    val songs = _songs.asStateFlow()

    private val _chartOnly = MutableStateFlow(false)
    private val chartOnly = _chartOnly.asStateFlow()

    fun setChartOnly(value: Boolean) {
        _chartOnly.value = value
    }

    init {
        viewModelScope.launch {
            _packs.value = repositoryContainer.packRepository.findAll().first()

            for (pack in packs.value) {
                var songs = repositoryContainer.songRepository.findBySet(pack.id).first()

                if (chartOnly.value) {
                    songs = songs.filter {
                        repositoryContainer.chartRepository.findAllBySongId(it.id).first()
                            .isNotEmpty()
                    }
                }

                if (songs.isNotEmpty()) {
                    packSongsMap[pack.id] = songs
                }
            }
        }
    }

    private fun setSongListBySet(set: String) {
        _songs.value = packSongsMap[set] ?: listOf()
    }

    private val _selectedPackIndex = MutableStateFlow(-1)
    val selectedPackIndex = _selectedPackIndex.asStateFlow()

    private val _selectedSongIndex = MutableStateFlow(-1)
    val selectedSongIndex = _selectedSongIndex.asStateFlow()

    val selectedSongId = selectedSongIndex.map {
        if (it > -1) songs.value[it].id else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun selectPackIndex(idx: Int) {
        _selectedPackIndex.value = idx
        _selectedSongIndex.value = -1

        setSongListBySet(packs.value[idx].id)
    }

    fun selectSongIndex(idx: Int) {
        _selectedSongIndex.value = idx
    }

    private fun reset() {
        _selectedPackIndex.value = -1
        _selectedSongIndex.value = -1
        _songs.value = listOf()
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

        val packIdx = packs.value.indexOfFirst { it.id == song.set }
        if (packIdx < 0) return
        selectPackIndex(packIdx)

        val songIdx = songs.value.indexOfFirst { it.id == song.id }
        if (songIdx < 0) return
        selectSongIndex(songIdx)
    }
}
