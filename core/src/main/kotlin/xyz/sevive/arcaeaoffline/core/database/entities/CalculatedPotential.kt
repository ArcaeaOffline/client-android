package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.DatabaseView

@DatabaseView(
    """
        SELECT
            avg(anon_1.b30_sum) AS b30 
        FROM (
            SELECT scores_best.potential AS b30_sum 
            FROM scores_best
            ORDER BY scores_best.potential DESC
            LIMIT 30
        ) AS anon_1
    """, "calculated_potential"
)
data class CalculatedPotential(
    val b30: Double,
)
