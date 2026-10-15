package xyz.sevive.arcaeaoffline.core.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

@Volatile
private var instance: ArcaeaOfflineDatabase? = null

private val instanceLock = Any()

fun getArcaeaOfflineDatabaseBuilder(context: Context): RoomDatabase.Builder<ArcaeaOfflineDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(ArcaeaOfflineDatabase.DATABASE_FILENAME)
    return Room.databaseBuilder<ArcaeaOfflineDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

fun getArcaeaOfflineDatabase(context: Context): ArcaeaOfflineDatabase {
    // If the instance is not null, return it, otherwise create a new database instance.
    return instance ?: synchronized(instanceLock) {
        getArcaeaOfflineDatabase(getArcaeaOfflineDatabaseBuilder(context)).also { instance = it }
    }
}
