package xyz.sevive.arcaeaoffline.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import xyz.sevive.arcaeaoffline.core.database.converters.InstantConverters
import xyz.sevive.arcaeaoffline.database.converters.DeviceOcrResultConverters
import xyz.sevive.arcaeaoffline.database.converters.OcrQueueStagingOptionsConverters
import xyz.sevive.arcaeaoffline.database.converters.OcrQueueStagingUriTypeConverters
import xyz.sevive.arcaeaoffline.database.converters.OcrQueueTaskStatusConverters
import xyz.sevive.arcaeaoffline.database.converters.PlayResultConverters
import xyz.sevive.arcaeaoffline.database.converters.UriConverters
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueStagingBatchDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueStagingItemDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueTaskDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingBatch
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingItem
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask
import xyz.sevive.arcaeaoffline.database.migrations.OcrQueueMigration3To4

@Database(
    entities = [OcrQueueTask::class, OcrQueueStagingItem::class, OcrQueueStagingBatch::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 3, to = 4, spec = OcrQueueMigration3To4::class),
    ],
)
@TypeConverters(
    UriConverters::class,
    InstantConverters::class,
    OcrQueueTaskStatusConverters::class,
    DeviceOcrResultConverters::class,
    PlayResultConverters::class,
    OcrQueueStagingOptionsConverters::class,
    OcrQueueStagingUriTypeConverters::class,
)
abstract class OcrQueueDatabase : RoomDatabase() {
    abstract fun ocrQueueTaskDao(): OcrQueueTaskDao

    abstract fun ocrQueueStagingItemDao(): OcrQueueStagingItemDao

    abstract fun ocrQueueStagingBatchDao(): OcrQueueStagingBatchDao

    companion object {
        const val DATABASE_FILENAME = "ocr-queue.db"

        @Volatile
        private var instance: OcrQueueDatabase? = null

        private fun getDatabaseBuilder(context: Context): Builder<OcrQueueDatabase> {
            val appContext = context.applicationContext
            val dbFile = appContext.getDatabasePath(DATABASE_FILENAME)
            return Room.databaseBuilder<OcrQueueDatabase>(
                context = appContext,
                name = dbFile.absolutePath,
            )
        }

        fun getDatabase(context: Context): OcrQueueDatabase {
            // If the instance is not null, return it, otherwise create a new database instance.
            return instance ?: synchronized(this) {
                getDatabaseBuilder(context)
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
