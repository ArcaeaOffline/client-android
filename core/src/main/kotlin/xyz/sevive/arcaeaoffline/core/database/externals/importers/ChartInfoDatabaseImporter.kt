package xyz.sevive.arcaeaoffline.core.database.externals.importers

import androidx.sqlite.SQLiteConnection
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.extensions.getIntOrNull


object ChartInfoDatabaseImporter {
    fun chartInfo(conn: SQLiteConnection): List<ChartInfo> {
        val items = mutableListOf<ChartInfo>()

        conn.prepare("SELECT `song_id`, `rating_class`, `constant`, `notes` FROM `charts_info`")
            .use { stmt ->
                while (stmt.step()) {
                    val chartInfo = ChartInfo(
                        songId = stmt.getText(0),
                        ratingClass = ArcaeaRatingClass.fromInt(stmt.getInt(1)),
                        constant = stmt.getInt(2),
                        notes = stmt.getIntOrNull(3),
                    )
                    items.add(chartInfo)
                }
            }

        return items
    }
}
