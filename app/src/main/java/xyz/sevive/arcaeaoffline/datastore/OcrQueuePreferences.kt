package xyz.sevive.arcaeaoffline.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


object OcrQueuePreferencesSerializer : Serializer<OcrQueuePreferences> {
    private fun applyDefaultValues(preferences: OcrQueuePreferences): OcrQueuePreferences {
        val builder = preferences.toBuilder()

        if (!builder.hasCheckIsImage()) builder.setCheckIsImage(true)
        if (!builder.hasCheckIsArcaeaImage()) builder.setCheckIsArcaeaImage(true)
        if (!builder.hasParallelCount()) builder.setParallelCount(
            Runtime.getRuntime().availableProcessors() / 2
        )

        return builder.build()
    }

    override val defaultValue: OcrQueuePreferences
        get() = applyDefaultValues(OcrQueuePreferences.getDefaultInstance())

    override suspend fun readFrom(input: InputStream): OcrQueuePreferences {
        try {
            return applyDefaultValues(OcrQueuePreferences.parseFrom(input))
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: OcrQueuePreferences, output: OutputStream) =
        t.writeTo(output)
}

class OcrQueuePreferencesRepository(private val dataStore: DataStore<OcrQueuePreferences>) {
    val preferencesFlow = dataStore.data

    suspend fun setCheckIsImage(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setCheckIsImage(value).build()
        }
    }

    suspend fun setCheckIsArcaeaImage(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setCheckIsArcaeaImage(value).build()
        }
    }

    suspend fun setParallelCount(value: Int) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setParallelCount(value).build()
        }
    }
}
