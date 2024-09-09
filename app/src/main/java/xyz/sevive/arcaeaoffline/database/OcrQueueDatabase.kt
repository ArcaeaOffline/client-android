package xyz.sevive.arcaeaoffline.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import xyz.sevive.arcaeaoffline.core.database.converters.InstantConverters
import xyz.sevive.arcaeaoffline.database.converters.ArcaeaPlayResultValidatorWarningsConverters
import xyz.sevive.arcaeaoffline.database.converters.DeviceOcrResultConverters
import xyz.sevive.arcaeaoffline.database.converters.ExceptionConverters
import xyz.sevive.arcaeaoffline.database.converters.OcrQueueTaskStatusConverters
import xyz.sevive.arcaeaoffline.database.converters.PlayResultConverters
import xyz.sevive.arcaeaoffline.database.converters.UriConverters
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueEnqueueBufferDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueTaskDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBuffer
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTask


@Database(
    entities = [OcrQueueTask::class, OcrQueueEnqueueBuffer::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    UriConverters::class,
    InstantConverters::class,
    OcrQueueTaskStatusConverters::class,
    DeviceOcrResultConverters::class,
    PlayResultConverters::class,
    ArcaeaPlayResultValidatorWarningsConverters::class,
    ExceptionConverters::class,
)
abstract class OcrQueueDatabase : RoomDatabase() {
    abstract fun ocrQueueTaskDao(): OcrQueueTaskDao
    abstract fun ocrQueueEnqueueBufferDao(): OcrQueueEnqueueBufferDao

    companion object {
        const val DATABASE_FILENAME = "ocr-queue.db"

        @Volatile
        private var instance: OcrQueueDatabase? = null

        fun getDatabase(context: Context): OcrQueueDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, OcrQueueDatabase::class.java, DATABASE_FILENAME)
                    .openHelperFactory(RequerySQLiteOpenHelperFactory()).build()
                    .also { instance = it }
            }
        }
    }
}

