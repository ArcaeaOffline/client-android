package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized

interface DifficultyLocalizedRepository {
    fun find(songId: String, ratingClass: ArcaeaRatingClass): Flow<DifficultyLocalized?>
    fun findAll(): Flow<List<DifficultyLocalized>>
    fun findAllBySongId(songId: String): Flow<List<DifficultyLocalized>>
    suspend fun insert(item: DifficultyLocalized): Long
    suspend fun insertAll(vararg items: DifficultyLocalized): List<Long>
    suspend fun insertAll(items: List<DifficultyLocalized>) = insertAll(*items.toTypedArray())
    suspend fun delete(item: DifficultyLocalized): Int
    suspend fun deleteAll(vararg items: DifficultyLocalized): Int
}

class DifficultyLocalizedRepositoryImpl(private val dao: DifficultyLocalizedDao) :
    DifficultyLocalizedRepository {
    override fun find(
        songId: String,
        ratingClass: ArcaeaRatingClass
    ): Flow<DifficultyLocalized?> =
        dao.find(songId, ratingClass)

    override fun findAll(): Flow<List<DifficultyLocalized>> = dao.findAll()

    override fun findAllBySongId(songId: String): Flow<List<DifficultyLocalized>> =
        dao.findAllBySongId(songId)

    override suspend fun insert(item: DifficultyLocalized) = dao.insert(item)

    override suspend fun insertAll(vararg items: DifficultyLocalized) = dao.insertAll(*items)

    override suspend fun delete(item: DifficultyLocalized) = dao.delete(item)

    override suspend fun deleteAll(vararg items: DifficultyLocalized) = dao.deleteAll(*items)
}
