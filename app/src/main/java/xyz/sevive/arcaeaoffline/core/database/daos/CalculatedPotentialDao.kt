package xyz.sevive.arcaeaoffline.core.database.daos

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface CalculatedPotentialDao {
    @Query("SELECT b30 FROM calculated_potential")
    fun b30(): Flow<Double>
}
