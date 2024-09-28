package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.daos.PropertyDao
import xyz.sevive.arcaeaoffline.core.database.entities.Property


interface PropertyRepository {
    fun find(key: String): Flow<Property?>
    suspend fun upsert(item: Property)
    suspend fun delete(item: Property)
    suspend fun delete(key: String)

    fun databaseVersion(): Flow<Int?>
    suspend fun setDatabaseVersion(ver: Int)

    suspend fun r30LastUpdatedAt(): Instant?
    suspend fun setR30LastUpdatedAt(instant: Instant)
    suspend fun deleteR30LastUpdatedAt()
}

class PropertyRepositoryImpl(private val dao: PropertyDao) : PropertyRepository {
    companion object {
        const val LOG_TAG = "PropertyRepoImpl"
    }

    override fun find(key: String): Flow<Property?> = dao.find(key)

    override suspend fun upsert(item: Property) = dao.upsert(item)

    override suspend fun delete(item: Property) = dao.delete(item)

    override suspend fun delete(key: String) = dao.delete(key)

    override fun databaseVersion(): Flow<Int?> {
        return this.find(Property.KEY_VERSION).map { it?.value?.toIntOrNull() }
    }

    override suspend fun setDatabaseVersion(ver: Int) {
        this.upsert(Property(Property.KEY_VERSION, ver.toString()))
    }

    override suspend fun r30LastUpdatedAt(): Instant? {
        val property = this.find(Property.KEY_R30_LAST_UPDATED_AT).firstOrNull() ?: return null
        return Instant.ofEpochMilli(property.value.toLong())
    }

    override suspend fun setR30LastUpdatedAt(instant: Instant) {
        this.upsert(Property(Property.KEY_R30_LAST_UPDATED_AT, instant.toEpochMilli().toString()))
    }

    override suspend fun deleteR30LastUpdatedAt() {
        this.delete(Property.KEY_R30_LAST_UPDATED_AT)
    }
}
