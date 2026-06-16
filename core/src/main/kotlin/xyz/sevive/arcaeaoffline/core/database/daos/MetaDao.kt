package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.room.RoomRawQuery

@Dao
interface MetaDao {
    @RawQuery
    suspend fun pragmaUserVersion(query: RoomRawQuery): Int
}
