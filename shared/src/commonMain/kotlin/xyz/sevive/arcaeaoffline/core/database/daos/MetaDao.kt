package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room3.Dao
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery

@Dao
interface MetaDao {
    @RawQuery
    suspend fun pragmaUserVersion(query: RoomRawQuery): Int
}
