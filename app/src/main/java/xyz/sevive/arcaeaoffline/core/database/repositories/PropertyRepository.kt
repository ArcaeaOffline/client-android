package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.PropertyDao
import xyz.sevive.arcaeaoffline.core.database.entities.Property


interface PropertyRepository {
    fun find(key: String): Flow<Property?>
    suspend fun upsert(item: Property)
    suspend fun delete(item: Property)
}

class PropertyRepositoryImpl(private val dao: PropertyDao) : PropertyRepository {
    override fun find(key: String): Flow<Property?> = dao.find(key)

    override suspend fun upsert(item: Property) = dao.upsert(item)

    override suspend fun delete(item: Property) = dao.delete(item)
}
