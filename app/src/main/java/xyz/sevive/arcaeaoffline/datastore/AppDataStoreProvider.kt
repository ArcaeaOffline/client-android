package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.preferencesDataStoreFile


object AppDataStoreProvider {
    private const val LOG_TAG = "AppDataStoreProvider"

    fun emergencyModePreferences(context: Context): DataStore<EmergencyModePreferences> {
        return DataStoreFactory.create(
            serializer = EmergencyModePreferencesSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler {
                Log.w(
                    LOG_TAG,
                    "Emergency mode activity preferences corrupt! Falling back to getDefaultInstance()"
                )
                EmergencyModePreferences.getDefaultInstance()
            },
            produceFile = { context.preferencesDataStoreFile("emergency_mode_activity_prefs.pb") },
        )
    }
}
