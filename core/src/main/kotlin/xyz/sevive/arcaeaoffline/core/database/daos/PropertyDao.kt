package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.Property


@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties WHERE `key` = :key")
    fun find(key: String): Flow<Property?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: Property)

    @Delete
    suspend fun delete(item: Property)

    @Query("DELETE FROM properties WHERE `key` = :key")
    suspend fun delete(key: String)
}

