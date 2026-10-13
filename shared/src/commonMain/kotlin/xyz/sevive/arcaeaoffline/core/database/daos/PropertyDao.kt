package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
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
