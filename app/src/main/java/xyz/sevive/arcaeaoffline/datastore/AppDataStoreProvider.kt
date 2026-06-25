package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.dataStoreFile
import co.touchlab.kermit.Logger
import okio.FileSystem
import okio.Path.Companion.toOkioPath

object AppDataStoreProvider {
    private const val LOG_TAG = "AppDataStoreProvider"
    private val logger = Logger.withTag(LOG_TAG)

    private fun logCorrupt(name: String) {
        logger.w { "$name corrupt! Falling back to its default value" }
    }

    fun appPreferences(context: Context): DataStore<AppPreferences> =
        DataStoreFactory.create(
            storage =
                OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = AppPreferencesSerializer,
                    producePath = { context.dataStoreFile("app_preferences.toml").toOkioPath() },
                ),
            corruptionHandler =
                ReplaceFileCorruptionHandler {
                    logCorrupt("AppPreferences")
                    AppPreferencesSerializer.defaultValue
                },
        )

    fun emergencyModePreferences(context: Context): DataStore<EmergencyModePreferences> =
        DataStoreFactory.create(
            storage =
                OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = EmergencyModePreferencesSerializer,
                    producePath = { context.dataStoreFile("emergency_mode_preferences.toml").toOkioPath() },
                ),
            corruptionHandler =
                ReplaceFileCorruptionHandler {
                    logCorrupt("EmergencyModePreferences")
                    EmergencyModePreferencesSerializer.defaultValue
                },
        )

    fun ocrQueuePreferences(context: Context): DataStore<OcrQueuePreferences> =
        DataStoreFactory.create(
            storage =
                OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = OcrQueuePreferencesSerializer,
                    producePath = { context.dataStoreFile("ocr_queue_preferences.toml").toOkioPath() },
                ),
            corruptionHandler =
                ReplaceFileCorruptionHandler {
                    logCorrupt("OcrQueuePreferences")
                    OcrQueuePreferencesSerializer.defaultValue
                },
        )

    fun unstableFlavorPreferences(context: Context): DataStore<UnstableFlavorPreferences> =
        DataStoreFactory.create(
            storage =
                OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = UnstableFlavorPreferencesSerializer,
                    producePath = { context.dataStoreFile("unstable_flavor_preferences.toml").toOkioPath() },
                ),
            corruptionHandler =
                ReplaceFileCorruptionHandler {
                    logCorrupt("UnstableFlavorPreferences")
                    UnstableFlavorPreferencesSerializer.defaultValue
                },
        )
}
