package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultCalculatedDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated

interface PlayResultCalculatedRepository {
    fun find(id: Int): Flow<PlayResultCalculated?>
    fun findAll(): Flow<List<PlayResultCalculated>>
    fun findAllBySongId(songId: String): Flow<List<PlayResultCalculated>>
}

class PlayResultCalculatedRepositoryImpl(
    private val dao: PlayResultCalculatedDao
) : PlayResultCalculatedRepository {
    override fun find(id: Int): Flow<PlayResultCalculated?> = dao.find(id)

    override fun findAll(): Flow<List<PlayResultCalculated>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<PlayResultCalculated>> =
        dao.findAllBySongId(songId)
}

