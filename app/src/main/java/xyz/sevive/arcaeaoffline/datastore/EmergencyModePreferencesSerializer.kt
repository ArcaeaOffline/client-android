package xyz.sevive.arcaeaoffline.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
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
