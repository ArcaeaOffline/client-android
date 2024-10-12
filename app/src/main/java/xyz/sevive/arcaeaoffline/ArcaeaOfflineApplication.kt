package xyz.sevive.arcaeaoffline

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHelper
import xyz.sevive.arcaeaoffline.ui.containers.AppDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl
import xyz.sevive.arcaeaoffline.ui.containers.DataStoreRepositoryContainerImpl
import xyz.sevive.arcaeaoffline.ui.containers.OcrQueueDatabaseRepositoryContainer


class ArcaeaOfflineApplication : Application() {
    companion object {
        const val LOG_TAG = "Application"
    }

    private val appScope =
        CoroutineScope(Dispatchers.Default) + CoroutineName("ArcaeaOfflineApplication")

    val arcaeaOfflineDatabaseRepositoryContainer by lazy {
        ArcaeaOfflineDatabaseRepositoryContainerImpl(this)
    }
    val appDatabaseRepositoryContainer by lazy { AppDatabaseRepositoryContainer(this) }
    val ocrQueueDatabaseRepositoryContainer by lazy { OcrQueueDatabaseRepositoryContainer(this) }
    val dataStoreRepositoryContainer by lazy { DataStoreRepositoryContainerImpl(this) }

    private val autoSendCrashReports by lazy {
        dataStoreRepositoryContainer.appPreferences.preferencesFlow.map {
            it.autoSendCrashReports
        }.stateIn(appScope, SharingStarted.Eagerly, false)
    }

    private val unstableAlertRead by lazy {
        dataStoreRepositoryContainer.unstableFlavorPreferences.preferencesFlow.map {
            it.unstableAlertRead
        }.stateIn(appScope, SharingStarted.Eagerly, null)
    }

    private fun addEmergencyModeShortcut() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return

        val intent = Intent(this, EmergencyModeActivity::class.java)
        intent.setAction(Intent.ACTION_VIEW)

        val shortcut = ShortcutInfo.Builder(this, "emergency_mode_activity")
            .setShortLabel(getString(R.string.shortcut_emergency_short_label))
            .setLongLabel(getString(R.string.shortcut_emergency_long_label))
            .setIcon(Icon.createWithResource(this, R.drawable.ic_activity_emergency_mode))
            .setIntent(intent)
            .build()

        shortcutManager.addDynamicShortcuts(listOf(shortcut))
    }

    private fun vacuumDatabases() {
        try {
            val databases = listOf(
                ArcaeaOfflineDatabase.getDatabase(this).openHelper.writableDatabase,
                OcrQueueDatabase.getDatabase(this).openHelper.writableDatabase,
            )

            databases.forEach { it.execSQL("VACUUM") }
            Log.i(LOG_TAG, "${databases.size} databases vacuumed")
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Error vacuuming databases", e)
        }
    }

    override fun onCreate() {
        super.onCreate()

        addEmergencyModeShortcut()

        AndroidThreeTen.init(this)

        SentryAndroid.init(this) {
            it.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                event.setExtra("unstable_alert_read", unstableAlertRead.value.toString())
                if (autoSendCrashReports.value) event else null
            }
        }

        appScope.launch(Dispatchers.IO) {
            DeviceOcrOnnxHelper.loadModelInfo(this@ArcaeaOfflineApplication)
            Notifications.createChannels(this@ArcaeaOfflineApplication)
            ArcaeaResourcesStateHelper.reloadStatus(this@ArcaeaOfflineApplication)
            vacuumDatabases()
        }
    }
}
