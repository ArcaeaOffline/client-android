package xyz.sevive.arcaeaoffline.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


object AppPreferencesSerializer : Serializer<AppPreferences> {
    private fun applyDefaultValues(preferences: AppPreferences): AppPreferences {
        val builder = preferences.toBuilder()

        if (!builder.hasAutoSendCrashReports()) builder.autoSendCrashReports = true

        return builder.build()
    }

    override val defaultValue: AppPreferences =
        this.applyDefaultValues(AppPreferences.getDefaultInstance())

    override suspend fun readFrom(input: InputStream): AppPreferences {
        try {
            return AppPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: AppPreferences, output: OutputStream) =
        t.writeTo(output)
}

class AppPreferencesRepository(
    private val dataStore: DataStore<AppPreferences>
) {
    val preferencesFlow = dataStore.data

    suspend fun setAutoSendCrashReports(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setAutoSendCrashReports(value).build()
        }
    }
}
