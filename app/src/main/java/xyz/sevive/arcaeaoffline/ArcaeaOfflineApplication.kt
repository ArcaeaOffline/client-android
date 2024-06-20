package xyz.sevive.arcaeaoffline

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.jakewharton.threetenabp.AndroidThreeTen
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender
import xyz.sevive.arcaeaoffline.ui.containers.AppDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl


class ArcaeaOfflineApplication : Application() {
    lateinit var arcaeaOfflineDatabaseRepositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
    lateinit var appDatabaseRepositoryContainer: AppDatabaseRepositoryContainer

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            // core configurations
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            deleteUnapprovedReportsOnApplicationStart = true

            // plugins
            httpSender {
                uri = BuildConfig.ACRA_HTTP_URI
                basicAuthLogin = BuildConfig.ACRA_HTTP_USERNAME
                basicAuthPassword = BuildConfig.ACRA_HTTP_PASSWORD
                httpMethod = HttpSender.Method.POST
            }

            dialog {
                title = "Uncaught Exception"
                reportDialogClass = CrashReportActivity::class.java
            }
        }
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

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        addEmergencyModeShortcut()
        arcaeaOfflineDatabaseRepositoryContainer =
            ArcaeaOfflineDatabaseRepositoryContainerImpl(this)
        appDatabaseRepositoryContainer = AppDatabaseRepositoryContainer(this)
    }
}
