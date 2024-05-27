package xyz.sevive.arcaeaoffline.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import xyz.sevive.arcaeaoffline.database.daos.OcrHistoryDao
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory


@Database(
    entities = [OcrHistory::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ocrHistoryDao(): OcrHistoryDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "app_data.db")
                    .openHelperFactory(RequerySQLiteOpenHelperFactory()).build()
                    .also { Instance = it }
            }
        }
    }
}
