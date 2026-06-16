package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object UnstableFlavorPreferencesSerializer : Serializer<UnstableFlavorPreferences> {
    override val defaultValue: UnstableFlavorPreferences =
        UnstableFlavorPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UnstableFlavorPreferences {
        try {
            return UnstableFlavorPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: UnstableFlavorPreferences,
        output: OutputStream,
    ) = t.writeTo(output)
}

class UnstableFlavorPreferencesRepository(
    context: Context,
) {
    private val dataStore = AppDataStoreProvider.unstableFlavorPreferences(context)
    val preferencesFlow = dataStore.data

    suspend fun setAlertRead(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toBuilder().setUnstableAlertRead(value).build()
        }
    }
}
