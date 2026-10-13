package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.ChartInfoDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.daos.SongDao
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated
import kotlin.uuid.Uuid

interface PlayResultCalculatedRepository {
    fun find(uuid: Uuid): Flow<PlayResultCalculated?>

    fun findAllByUuid(uuids: List<Uuid>): Flow<List<PlayResultCalculated>>

    fun findAllBySongIdAndRatingClass(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<List<PlayResultCalculated>>
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayResultCalculatedRepositoryImpl(
    private val playResultDao: PlayResultDao,
    private val songDao: SongDao,
    private val chartInfoDao: ChartInfoDao,
) : PlayResultCalculatedRepository {
    private suspend fun calculatePlayResult(playResult: PlayResult?): PlayResultCalculated? {
        if (playResult == null) return null

        val isDeletedInGame =
            songDao.isDeletedInGame(playResult.songId).firstOrNull() ?: return null
        if (isDeletedInGame) return null

        val chartInfo =
            chartInfoDao.find(playResult.songId, playResult.ratingClass).firstOrNull()
                ?: return null

        return PlayResultCalculated(playResult, chartInfo)
    }

    private suspend fun mapPlayResults(playResults: List<PlayResult>) = playResults.mapNotNull { calculatePlayResult(it) }

    override fun find(uuid: Uuid): Flow<PlayResultCalculated?> = playResultDao.findByUuid(uuid).mapLatest { calculatePlayResult(it) }

    override fun findAllByUuid(uuids: List<Uuid>): Flow<List<PlayResultCalculated>> =
        playResultDao.findAllByUuid(uuids).mapLatest { mapPlayResults(it) }

    override fun findAllBySongIdAndRatingClass(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<List<PlayResultCalculated>> =
        playResultDao
            .findAllBySongIdAndRatingClass(songId, ratingClass)
            .mapLatest { mapPlayResults(it) }
}
