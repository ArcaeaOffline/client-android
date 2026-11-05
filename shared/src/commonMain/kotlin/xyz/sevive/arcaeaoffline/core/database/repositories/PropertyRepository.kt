package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.daos.PropertyDao
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import kotlin.time.Instant

interface PropertyRepository {
    companion object {
        /** The default mode when database has no corresponding property, or one the app cannot recognize. */
        val DEFAULT_SCORING_MODE = ArcaeaScoringMode.B50
    }

    fun find(key: String): Flow<Property?>

    suspend fun upsert(item: Property)

    suspend fun delete(item: Property)

    suspend fun delete(key: String)

    fun databaseVersion(): Flow<Int?>

    suspend fun setDatabaseVersion(ver: Int)

    fun scoringMode(): Flow<ArcaeaScoringMode>

    suspend fun setScoringMode(mode: ArcaeaScoringMode)

    suspend fun r30LastUpdatedAt(): Instant?

    suspend fun setR30LastUpdatedAt(instant: Instant)

    fun packlistLastImportedAt(): Flow<Instant?>

    suspend fun setPacklistLastImportedAt(instant: Instant)

    fun songsLastImportedAt(): Flow<Instant?>

    suspend fun setSongsLastImportedAt(instant: Instant)

    fun chartInfoLastImportedAt(): Flow<Instant?>

    suspend fun setChartInfoLastImportedAt(instant: Instant)
}

class PropertyRepositoryImpl(
    private val dao: PropertyDao,
) : PropertyRepository {
    companion object {
        const val LOG_TAG = "PropertyRepoImpl"
    }

    override fun find(key: String): Flow<Property?> = dao.find(key)

    override suspend fun upsert(item: Property) = dao.upsert(item)

    override suspend fun delete(item: Property) = dao.delete(item)

    override suspend fun delete(key: String) = dao.delete(key)

    override fun databaseVersion(): Flow<Int?> = this.find(Property.KEY_VERSION).map { it?.value?.toIntOrNull() }

    override suspend fun setDatabaseVersion(ver: Int) {
        this.upsert(Property(Property.KEY_VERSION, ver.toString()))
    }

    // The scoring mode describes how this database's play results are
    // interpreted, so it lives in the database itself and travels with the
    // database file. Falls back to the latest mode when unset or unknown.
    override fun scoringMode(): Flow<ArcaeaScoringMode> =
        this.find(Property.KEY_SCORING_MODE).map { property ->
            property?.value?.toIntOrNull()?.let { ArcaeaScoringMode.fromKey(it) }
                ?: PropertyRepository.DEFAULT_SCORING_MODE
        }

    override suspend fun setScoringMode(mode: ArcaeaScoringMode) {
        this.upsert(Property(Property.KEY_SCORING_MODE, mode.key.toString()))
    }

    override suspend fun r30LastUpdatedAt(): Instant? {
        val property = this.find(Property.KEY_R30_LAST_UPDATED_AT).firstOrNull() ?: return null
        return Instant.fromEpochMilliseconds(property.value.toLong())
    }

    override suspend fun setR30LastUpdatedAt(instant: Instant) {
        this.upsert(Property(Property.KEY_R30_LAST_UPDATED_AT, instant.toEpochMilliseconds().toString()))
    }

    private fun lastImportedAt(key: String): Flow<Instant?> =
        this.find(key).map { it?.value?.toLongOrNull()?.let(Instant::fromEpochMilliseconds) }

    private suspend fun setLastImportedAt(
        key: String,
        instant: Instant,
    ) {
        this.upsert(Property(key, instant.toEpochMilliseconds().toString()))
    }

    override fun packlistLastImportedAt(): Flow<Instant?> = lastImportedAt(Property.KEY_PACKLIST_LAST_IMPORTED_AT)

    override suspend fun setPacklistLastImportedAt(instant: Instant) = setLastImportedAt(Property.KEY_PACKLIST_LAST_IMPORTED_AT, instant)

    override fun songsLastImportedAt(): Flow<Instant?> = lastImportedAt(Property.KEY_SONGS_LAST_IMPORTED_AT)

    override suspend fun setSongsLastImportedAt(instant: Instant) = setLastImportedAt(Property.KEY_SONGS_LAST_IMPORTED_AT, instant)

    override fun chartInfoLastImportedAt(): Flow<Instant?> = lastImportedAt(Property.KEY_CHART_INFO_LAST_IMPORTED_AT)

    override suspend fun setChartInfoLastImportedAt(instant: Instant) = setLastImportedAt(Property.KEY_CHART_INFO_LAST_IMPORTED_AT, instant)
}
