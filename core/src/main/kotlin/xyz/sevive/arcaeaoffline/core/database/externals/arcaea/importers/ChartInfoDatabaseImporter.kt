package xyz.sevive.arcaeaoffline.core.database.externals.arcaea.importers

import androidx.core.database.getIntOrNull
import io.requery.android.database.sqlite.SQLiteDatabase
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo


object ChartInfoDatabaseImporter {
    fun chartInfo(db: SQLiteDatabase): List<ChartInfo> {
        val items = mutableListOf<ChartInfo>()

        val cursor = db.query(
            "charts_info",
            arrayOf("song_id", "rating_class", "constant", "notes"),
            null,
            null,
            null,
            null,
            null,
        )

        if (!cursor.moveToFirst()) return emptyList()
        cursor.use {
            do {
                val songId = it.getString(it.getColumnIndexOrThrow("song_id"))
                val ratingClass = it.getInt(it.getColumnIndexOrThrow("rating_class"))
                val constant = it.getInt(it.getColumnIndexOrThrow("constant"))
                val notes = it.getIntOrNull(it.getColumnIndex("notes"))

                val chartInfo = ChartInfo(
                    songId = songId,
                    ratingClass = ArcaeaRatingClass.fromInt(ratingClass),
                    constant = constant,
                    notes = notes,
                )
                items.add(chartInfo)
            } while (it.moveToNext())
        }

        return items
    }
}
