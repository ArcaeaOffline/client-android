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


class SongIdSelectorViewModel(private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer) :
    ViewModel() {

    val packList = repositoryContainer.packRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val basePackMap = packList.map { packs ->
        packs.mapNotNull {
            val basePack = repositoryContainer.packRepository.findBasePack(it.id).firstOrNull()
            if (basePack != null) (it.id to basePack) else null
        }.toMap()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = mapOf()
    )

    private val _songList = MutableStateFlow<List<Song>>(listOf())
    val songList = _songList.asStateFlow()

    suspend fun setSongListBySet(set: String, chartOnly: Boolean = false) {
        _songList.value = repositoryContainer.songRepository.findBySet(set).map { songs ->
            if (chartOnly) {
                songs.filter {
                    repositoryContainer.chartRepository.findAllBySongId(it.id).first().isNotEmpty()
                }
            } else songs
        }.first()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5000L
    }
}
