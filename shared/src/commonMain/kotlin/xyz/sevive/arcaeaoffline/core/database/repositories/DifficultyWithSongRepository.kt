package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyWithSongDao
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSongAndInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

interface DifficultyWithSongRepository {
    fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<DifficultyWithSong?>

    fun find(playResult: PlayResult): Flow<DifficultyWithSong?> = find(playResult.songId, playResult.ratingClass)

    fun findAllBySongId(songId: String): Flow<List<DifficultyWithSong>>

    fun findAllBySongIds(songIds: List<String>): Flow<List<DifficultyWithSong>>

    fun findAllWithInfo(): Flow<List<DifficultyWithSongAndInfo>>
}

class DifficultyWithSongRepositoryImpl(
    private val dao: DifficultyWithSongDao,
) : DifficultyWithSongRepository {
    override fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass,
    ): Flow<DifficultyWithSong?> = dao.find(songId, ratingClass)

    override fun findAllBySongId(songId: String): Flow<List<DifficultyWithSong>> = dao.findAllBySongId(songId)

    override fun findAllBySongIds(songIds: List<String>): Flow<List<DifficultyWithSong>> = dao.findAllBySongIds(songIds)

    override fun findAllWithInfo(): Flow<List<DifficultyWithSongAndInfo>> = dao.findAllWithInfo()
}
