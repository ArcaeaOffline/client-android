package xyz.sevive.arcaeaoffline.datastore

import androidx.datastore.core.DataStore
import androidx.documentfile.provider.DocumentFile


class EmergencyModePreferencesRepository(
    private val dataStore: DataStore<EmergencyModePreferences>
) {
    val preferencesFlow = dataStore.data

    suspend fun updateLastOutputDirectory(documentFile: DocumentFile) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setLastOutputDirectory(documentFile.uri.toString()).build()
        }
    }
}

