package xyz.sevive.arcaeaoffline.ui.containers

import android.content.Context
import xyz.sevive.arcaeaoffline.datastore.AppDataStoreProvider
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.UnstableFlavorPreferencesRepository

interface DataStoreRepositoryContainer {
    val emergencyModePreferences: EmergencyModePreferencesRepository
    val ocrQueuePreferences: OcrQueuePreferencesRepository
    val unstableFlavorPreferences: UnstableFlavorPreferencesRepository
}

class DataStoreRepositoryContainerImpl(
    private val context: Context
) : DataStoreRepositoryContainer {
    override val emergencyModePreferences: EmergencyModePreferencesRepository by lazy {
        EmergencyModePreferencesRepository(AppDataStoreProvider.emergencyModePreferences(context))
    }

    override val ocrQueuePreferences: OcrQueuePreferencesRepository by lazy {
        OcrQueuePreferencesRepository(AppDataStoreProvider.ocrQueuePreferences(context))
    }

    override val unstableFlavorPreferences: UnstableFlavorPreferencesRepository by lazy {
        UnstableFlavorPreferencesRepository(AppDataStoreProvider.unstableFlavorPreferences(context))
    }
}
