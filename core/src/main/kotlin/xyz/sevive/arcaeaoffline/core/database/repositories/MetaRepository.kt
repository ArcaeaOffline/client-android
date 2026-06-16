package xyz.sevive.arcaeaoffline.core.database.repositories

import androidx.room.RoomRawQuery
import xyz.sevive.arcaeaoffline.core.database.daos.MetaDao

interface MetaRepository {
    suspend fun schemaVersion(): Int
}

class MetaRepositoryImpl(
    private val dao: MetaDao,
) : MetaRepository {
    override suspend fun schemaVersion() =
        dao.pragmaUserVersion(
            RoomRawQuery("PRAGMA user_version"),
        )
}
