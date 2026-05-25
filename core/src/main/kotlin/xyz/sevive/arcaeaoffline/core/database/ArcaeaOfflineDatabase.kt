package xyz.sevive.arcaeaoffline.core.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import xyz.sevive.arcaeaoffline.core.database.converters.ArcaeaLanguageConverters
import xyz.sevive.arcaeaoffline.core.database.converters.ArcaeaPlayResultClearTypeConverters
import xyz.sevive.arcaeaoffline.core.database.converters.ArcaeaPlayResultModifierConverters
import xyz.sevive.arcaeaoffline.core.database.converters.ArcaeaRatingClassConverters
import xyz.sevive.arcaeaoffline.core.database.converters.InstantConverters
import xyz.sevive.arcaeaoffline.core.database.converters.UUIDByteArrayConverters
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.daos.ChartInfoDao
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyDao
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.daos.PackDao
import xyz.sevive.arcaeaoffline.core.database.daos.PackLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.daos.PropertyDao
import xyz.sevive.arcaeaoffline.core.database.daos.R30EntryDao
import xyz.sevive.arcaeaoffline.core.database.daos.RelationshipsDao
import xyz.sevive.arcaeaoffline.core.database.daos.SongDao
import xyz.sevive.arcaeaoffline.core.database.daos.SongLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized
import xyz.sevive.arcaeaoffline.core.database.migrations.AutoMigration_5_6
import xyz.sevive.arcaeaoffline.core.database.migrations.AutoMigration_9_10
import xyz.sevive.arcaeaoffline.core.database.migrations.Migration_6_7
import xyz.sevive.arcaeaoffline.core.database.migrations.Migration_7_8

@Database(
    entities = [
        Property::class,
        Pack::class,
        PackLocalized::class,
        Song::class,
        SongLocalized::class,
        Difficulty::class,
        DifficultyLocalized::class,
        ChartInfo::class,
        PlayResult::class,
        R30Entry::class,
    ],
    views = [Chart::class],
    autoMigrations = [
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = AutoMigration_5_6::class),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10, spec = AutoMigration_9_10::class),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
    ],
    version = 14,
    exportSchema = true,
)
@TypeConverters(
    InstantConverters::class,
    UUIDByteArrayConverters::class,
    ArcaeaRatingClassConverters::class,
    ArcaeaPlayResultClearTypeConverters::class,
    ArcaeaPlayResultModifierConverters::class,
    ArcaeaLanguageConverters::class,
)
abstract class ArcaeaOfflineDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao

    abstract fun packDao(): PackDao

    abstract fun packLocalizedDao(): PackLocalizedDao

    abstract fun songDao(): SongDao

    abstract fun songLocalizedDao(): SongLocalizedDao

    abstract fun difficultyDao(): DifficultyDao

    abstract fun difficultyLocalizedDao(): DifficultyLocalizedDao

    abstract fun chartInfoDao(): ChartInfoDao

    abstract fun playResultDao(): PlayResultDao

    abstract fun relationshipsDao(): RelationshipsDao

    abstract fun r30EntryDao(): R30EntryDao

    abstract fun chartDao(): ChartDao

    companion object {
        const val DATABASE_FILENAME = "arcaea_offline.db"

        @Volatile
        private var instance: ArcaeaOfflineDatabase? = null

        private fun getDatabaseBuilder(context: Context): Builder<ArcaeaOfflineDatabase> {
            val appContext = context.applicationContext
            val dbFile = appContext.getDatabasePath(DATABASE_FILENAME)
            return Room.databaseBuilder<ArcaeaOfflineDatabase>(
                context = appContext,
                name = dbFile.absolutePath,
            )
        }

        fun getDatabase(context: Context): ArcaeaOfflineDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return instance ?: synchronized(this) {
                getDatabaseBuilder(context)
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(Dispatchers.IO)
                    .addMigrations(
                        Migration_6_7,
                        Migration_7_8,
                    ).build()
                    .also { instance = it }
            }
        }
    }
}
