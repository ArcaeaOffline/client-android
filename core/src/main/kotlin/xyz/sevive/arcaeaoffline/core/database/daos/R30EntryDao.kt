package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.entities.R30EntryAndPlayResult

@Dao
interface R30EntryDao {
    @Transaction
    @Query("SELECT * FROM r30_entries")
    fun findAll(): Flow<List<R30EntryAndPlayResult>>

    @Insert
    fun insert(item: R30Entry)

    @Insert
    fun insertAll(vararg items: R30Entry)

    @Delete
    fun delete(item: R30Entry)

    @Delete
    fun deleteAll(vararg items: R30Entry)

    /**
     * ```sql
     * DELETE FROM r30_entries
     * ```
     * CAUTION! ONLY CALL THIS WHEN YOU KNOW WHAT YOU ARE DOING!
     */
    @Query("DELETE FROM r30_entries")
    fun emptyTable()
}
