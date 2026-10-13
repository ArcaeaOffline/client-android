package xyz.sevive.arcaeaoffline.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.databasesDir
import io.github.vinceglb.filekit.div

fun getArcaeaOfflineDatabaseBuilder(): RoomDatabase.Builder<ArcaeaOfflineDatabase> =
    Room.databaseBuilder<ArcaeaOfflineDatabase>(
        name = (FileKit.databasesDir / ArcaeaOfflineDatabase.DATABASE_FILENAME).absolutePath(),
    )
