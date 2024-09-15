package xyz.sevive.arcaeaoffline

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
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
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.helpers.GlobalArcaeaButtonStateHelper
import xyz.sevive.arcaeaoffline.ui.containers.AppDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl
import xyz.sevive.arcaeaoffline.ui.containers.DataStoreRepositoryContainerImpl
import xyz.sevive.arcaeaoffline.ui.containers.OcrQueueDatabaseRepositoryContainer


class ArcaeaOfflineApplication : Application() {
    private val appScope =
        CoroutineScope(Dispatchers.Default) + CoroutineName("ArcaeaOfflineApplication")

    lateinit var arcaeaOfflineDatabaseRepositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
    lateinit var appDatabaseRepositoryContainer: AppDatabaseRepositoryContainer
    val ocrQueueDatabaseRepositoryContainer by lazy { OcrQueueDatabaseRepositoryContainer(this) }
    val dataStoreRepositoryContainer by lazy { DataStoreRepositoryContainerImpl(this) }

    private val enableSentry = dataStoreRepositoryContainer.appPreferences.preferencesFlow.map {
        it.enableSentry
    }.stateIn(appScope, SharingStarted.Eagerly, false)

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

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        SentryAndroid.init(this) {
            it.beforeSend = SentryOptions.BeforeSendCallback { event, hint ->
                if (enableSentry.value) event else null
            }
        }

        appScope.launch {
            DeviceOcrOnnxHelper.loadModelInfo(this@ArcaeaOfflineApplication)
            Notifications.createChannels(this@ArcaeaOfflineApplication)
            GlobalArcaeaButtonStateHelper.reload(this@ArcaeaOfflineApplication)
        }

        addEmergencyModeShortcut()
        arcaeaOfflineDatabaseRepositoryContainer =
            ArcaeaOfflineDatabaseRepositoryContainerImpl(this)
        appDatabaseRepositoryContainer = AppDatabaseRepositoryContainer(this)
    }
}
