package xyz.sevive.arcaeaoffline.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration
import io.requery.android.database.sqlite.SQLiteFunction
import xyz.sevive.arcaeaoffline.core.database.daos.CalculatedPotentialDao
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.daos.ChartInfoDao
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyDao
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.daos.PackDao
import xyz.sevive.arcaeaoffline.core.database.daos.PackLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.daos.PropertyDao
import xyz.sevive.arcaeaoffline.core.database.daos.ScoreBestDao
import xyz.sevive.arcaeaoffline.core.database.daos.ScoreCalculatedDao
import xyz.sevive.arcaeaoffline.core.database.daos.ScoreDao
import xyz.sevive.arcaeaoffline.core.database.daos.SongDao
import xyz.sevive.arcaeaoffline.core.database.daos.SongLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.entities.CalculatedPotential
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreBest
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreCalculated
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized
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
        Score::class,
    ],
    views = [Chart::class, ScoreCalculated::class, ScoreBest::class, CalculatedPotential::class],
    version = 4
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
    abstract fun scoreDao(): ScoreDao

    abstract fun chartDao(): ChartDao
    abstract fun scoreCalculatedDao(): ScoreCalculatedDao
    abstract fun scoreBestDao(): ScoreBestDao

    abstract fun calculatedPotentialDao(): CalculatedPotentialDao

    companion object {
        private const val DATABASE_NAME = "arcaea_offline.db"

        @Volatile
        private var Instance: ArcaeaOfflineDatabase? = null

        fun getDatabase(context: Context): ArcaeaOfflineDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context, ArcaeaOfflineDatabase::class.java, DATABASE_NAME
                ).openHelperFactory { configuration ->
                    // Custom Functions on Android SQLite with Room
                    // https://medium.com/@adarshsharma1904/custom-functions-on-android-sqlite-with-room-e79b53c4c924
                    val config = SQLiteDatabaseConfiguration(
                        context.getDatabasePath(DATABASE_NAME).path,
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
                }.build().also { Instance = it }
            }
        }
    }
}
