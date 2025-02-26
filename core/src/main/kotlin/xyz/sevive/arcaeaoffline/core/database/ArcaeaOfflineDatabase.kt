package xyz.sevive.arcaeaoffline.core.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import io.requery.android.database.sqlite.SQLiteFunction
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
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultBestDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultCalculatedDao
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
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultCalculated
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.core.database.entities.R30Entry
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized
import xyz.sevive.arcaeaoffline.core.database.migrations.AutoMigration_5_6
import xyz.sevive.arcaeaoffline.core.database.migrations.AutoMigration_9_10
import xyz.sevive.arcaeaoffline.core.database.migrations.Migration_6_7
import xyz.sevive.arcaeaoffline.core.database.migrations.Migration_7_8
import kotlin.math.floor


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
    views = [Chart::class, PlayResultCalculated::class, PlayResultBest::class],
    autoMigrations = [
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = AutoMigration_5_6::class),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10, spec = AutoMigration_9_10::class),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
    ],
    version = 12,
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
    abstract fun playResultCalculatedDao(): PlayResultCalculatedDao
    abstract fun playResultBestDao(): PlayResultBestDao

    companion object {
        const val DATABASE_FILENAME = "arcaea_offline.db"

        @Volatile
        private var Instance: ArcaeaOfflineDatabase? = null

        fun getDatabase(context: Context): ArcaeaOfflineDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, ArcaeaOfflineDatabase::class.java, DATABASE_FILENAME
                ).openHelperFactory { configuration ->
                    // Custom Functions on Android SQLite with Room
                    // https://medium.com/@adarshsharma1904/custom-functions-on-android-sqlite-with-room-e79b53c4c924
                    val config = SQLiteDatabaseConfiguration(
                        context.getDatabasePath(DATABASE_FILENAME).path,
                        SQLiteDatabase.OPEN_CREATE or SQLiteDatabase.OPEN_READWRITE
                    )

                    config.functions.add(SQLiteFunction("FLOOR", 1) { args, result ->
                        if (args != null && result != null) {
                            val number = args.getDouble(0)
                            result.set(floor(number).toLong())
                        }
                    })

                    val options = RequerySQLiteOpenHelperFactory.ConfigurationOptions { config }
                    RequerySQLiteOpenHelperFactory(listOf(options)).create(configuration)
                }.addMigrations(
                    Migration_6_7,
                    Migration_7_8,
                ).build().also { Instance = it }
            }
        }
    }
}
