package xyz.sevive.arcaeaoffline

import android.app.Application
import android.content.Context
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

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        arcaeaOfflineDatabaseRepositoryContainer =
            ArcaeaOfflineDatabaseRepositoryContainerImpl(this)
        appDatabaseRepositoryContainer = AppDatabaseRepositoryContainer(this)
    }
}
