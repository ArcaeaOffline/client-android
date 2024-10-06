package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.preferencesDataStoreFile


object AppDataStoreProvider {
    private const val LOG_TAG = "AppDataStoreProvider"

    private fun logCorrupt(name: String) {
        Log.w(LOG_TAG, "${name} corrupt! Falling back to its default value")
    }

    fun appPreferences(context: Context): DataStore<AppPreferences> {
        return DataStoreFactory.create(
            serializer = AppPreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler {
                logCorrupt("AppPreferences")
                AppPreferencesSerializer.defaultValue
            },
            produceFile = { context.preferencesDataStoreFile("app_prefs") },
        )
    }

    fun emergencyModePreferences(context: Context): DataStore<EmergencyModePreferences> {
        return DataStoreFactory.create(
            serializer = EmergencyModePreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler {
                logCorrupt("EmergencyModePreferences")
                EmergencyModePreferencesSerializer.defaultValue
            },
            produceFile = { context.preferencesDataStoreFile("emergency_mode_activity_prefs.pb") },
        )
    }

    fun ocrQueuePreferences(context: Context): DataStore<OcrQueuePreferences> {
        return DataStoreFactory.create(
            serializer = OcrQueuePreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler {
                logCorrupt("OcrQueuePreferences")
                OcrQueuePreferencesSerializer.defaultValue
            },
            produceFile = { context.preferencesDataStoreFile("ocr_queue_prefs.pb") },
        )
    }

    fun unstableFlavorPreferences(context: Context): DataStore<UnstableFlavorPreferences> {
        return DataStoreFactory.create(
            serializer = UnstableFlavorPreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler {
                logCorrupt("UnstableFlavorPreferences")
                UnstableFlavorPreferencesSerializer.defaultValue
            },
            produceFile = { context.preferencesDataStoreFile("unstable_flavor_prefs") },
        )
    }
}
