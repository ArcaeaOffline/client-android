package xyz.sevive.arcaeaoffline.ui.ocr.queue

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException


val Context.ocrQueueDataStore: DataStore<Preferences> by preferencesDataStore(name = "ocr-queue")

data class OcrQueuePreferences(
    val checkIsImage: Boolean,
    val checkIsArcaeaImage: Boolean,
    val channelCapacity: Int?,
    val parallelCount: Int?,
)

class OcrQueuePreferencesRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        const val LOG_TAG = "OcrQueuePrefRepo"
    }

    private object PreferencesKeys {
        val CHECK_IS_IMAGE = booleanPreferencesKey("check_is_image")
        val CHECK_IS_ARCAEA_IMAGE = booleanPreferencesKey("check_is_arcaea_image")
        val CHANNEL_CAPACITY = intPreferencesKey("channel_capacity")
        val PARALLEL_COUNT = intPreferencesKey("parallel_count")
    }

    val preferencesFlow: Flow<OcrQueuePreferences> = dataStore.data.catch { e ->
        if (e is IOException) {
            Log.e(LOG_TAG, "Error reading preferences.", e)
            emit(emptyPreferences())
        } else {
            throw e
        }
    }.map { preferences ->
        mapPreferences(preferences)
    }

    suspend fun setCheckIsImage(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHECK_IS_IMAGE] = value
        }
    }

    suspend fun setCheckIsArcaeaImage(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHECK_IS_ARCAEA_IMAGE] = value
        }
    }

    suspend fun setChannelCapacity(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CHANNEL_CAPACITY] = value
        }
    }

    suspend fun setParallelCount(value: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PARALLEL_COUNT] = value
        }
    }

    suspend fun fetchInitialPreferences() = mapPreferences(dataStore.data.first().toPreferences())

    private fun mapPreferences(preferences: Preferences): OcrQueuePreferences {
        val checkIsImage = preferences[PreferencesKeys.CHECK_IS_IMAGE] ?: true
        val checkIsArcaeaImage = preferences[PreferencesKeys.CHECK_IS_ARCAEA_IMAGE] ?: true
        val channelCapacity = preferences[PreferencesKeys.CHANNEL_CAPACITY]
        val parallelCount = preferences[PreferencesKeys.PARALLEL_COUNT]

        return OcrQueuePreferences(
            checkIsImage = checkIsImage,
            checkIsArcaeaImage = checkIsArcaeaImage,
            channelCapacity = channelCapacity,
            parallelCount = parallelCount,
        )
    }
}
