package xyz.sevive.arcaeaoffline

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.room.execSQL
import androidx.room.useWriterConnection
import co.touchlab.kermit.Logger
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.maintenance.AppDataMaintenanceManager
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.datastore.AppPreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.UnstableFlavorPreferencesRepository
import xyz.sevive.arcaeaoffline.di.appModule
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHolder

class ArcaeaOfflineApplication : Application() {
    companion object {
        const val LOG_TAG = "Application"
    }

    private val logger = Logger.withTag(LOG_TAG)

    private val appScope =
        CoroutineScope(Dispatchers.Default + SupervisorJob()) + CoroutineName("ArcaeaOfflineApplication")

    private fun addEmergencyModeShortcut() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = getSystemService(ShortcutManager::class.java) ?: return

        val intent = Intent(this, EmergencyModeActivity::class.java)
        intent.setAction(Intent.ACTION_VIEW)

        val shortcut =
            ShortcutInfo
                .Builder(this, "emergency_mode_activity")
                .setShortLabel(getString(R.string.shortcut_emergency_short_label))
                .setLongLabel(getString(R.string.shortcut_emergency_long_label))
                .setIcon(Icon.createWithResource(this, R.drawable.ic_activity_emergency_mode))
                .setIntent(intent)
                .build()

        shortcutManager.addDynamicShortcuts(listOf(shortcut))
    }

    private suspend fun vacuumDatabases() {
        try {
            val databases =
                listOf(
                    ArcaeaOfflineDatabase.getDatabase(this),
                    OcrQueueDatabase.getDatabase(this),
                )

            databases.forEach { db ->
                db.useWriterConnection {
                    it.execSQL("VACUUM")
                }
            }
            logger.i { "${databases.size} databases vacuumed" }
        } catch (e: Exception) {
            logger.w(e) { "Error vacuuming databases" }
        }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ArcaeaOfflineApplication)
            workManagerFactory()
            modules(appModule)
        }

        addEmergencyModeShortcut()

        val appPreferencesRepo by lazy { getKoin().get<AppPreferencesRepository>() }
        val unstableFlavorRepo by lazy { getKoin().get<UnstableFlavorPreferencesRepository>() }

        val autoSendCrashReports =
            appPreferencesRepo.preferencesFlow
                .map { it.autoSendCrashReports }
                .stateIn(appScope, SharingStarted.Eagerly, false)

        val unstableAlertRead =
            unstableFlavorRepo.preferencesFlow
                .map { it.unstableAlertRead }
                .stateIn(appScope, SharingStarted.Eagerly, null)

        SentryAndroid.init(this) {
            it.beforeSend =
                SentryOptions.BeforeSendCallback { event, _ ->
                    event.setExtra("unstable_alert_read", unstableAlertRead.value.toString())
                    val shouldSend = autoSendCrashReports.value
                    logger.d { "Sentry beforeSend: autoSend is $shouldSend" }
                    if (shouldSend) event else null
                }

            it.isDebug = BuildConfig.DEBUG
        }

        appScope.launch(Dispatchers.IO) {
            AppDataMaintenanceManager(this@ArcaeaOfflineApplication).runAllTasks()
            DeviceOcrOnnxHelper.loadModelInfo(this@ArcaeaOfflineApplication)
            Notifications.createChannels(this@ArcaeaOfflineApplication)
            ArcaeaResourcesStateHolder.reloadStatus(this@ArcaeaOfflineApplication)
            vacuumDatabases()
        }
    }
}
