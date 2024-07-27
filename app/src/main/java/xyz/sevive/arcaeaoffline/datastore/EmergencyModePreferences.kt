package xyz.sevive.arcaeaoffline.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import androidx.documentfile.provider.DocumentFile
import java.io.InputStream
import java.io.OutputStream


object EmergencyModePreferencesSerializer : Serializer<EmergencyModePreferences> {
    override val defaultValue: EmergencyModePreferences =
        EmergencyModePreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): EmergencyModePreferences {
        try {
            return EmergencyModePreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: EmergencyModePreferences, output: OutputStream) =
        t.writeTo(output)
}

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
