package xyz.sevive.arcaeaoffline.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import xyz.sevive.arcaeaoffline.core.database.converters.InstantConverters
import xyz.sevive.arcaeaoffline.database.daos.OcrHistoryDao
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory


@Database(
    entities = [OcrHistory::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    InstantConverters::class,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ocrHistoryDao(): OcrHistoryDao

    companion object {
        const val DATABASE_FILENAME = "app_data.db"

        @Volatile
        private var Instance: AppDatabase? = null

        private fun getDatabaseBuilder(context: Context): Builder<AppDatabase> {
            val appContext = context.applicationContext
            val dbFile = appContext.getDatabasePath(DATABASE_FILENAME)
            return Room.databaseBuilder<AppDatabase>(
                context = appContext,
                name = dbFile.absolutePath,
            )
        }


        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                getDatabaseBuilder(context)
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { Instance = it }
            }
        }
    }
}
