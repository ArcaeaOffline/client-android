package xyz.sevive.arcaeaoffline.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


object AppPreferencesSerializer : Serializer<AppPreferences> {
    override val defaultValue: AppPreferences = AppPreferences.getDefaultInstance()

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

    suspend fun setEnableSentry(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setEnableSentry(value).build()
        }
    }
}
