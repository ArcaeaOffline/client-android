package xyz.sevive.arcaeaoffline

import android.app.Application
import android.content.Context
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender

class ArcaeaOfflineApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            // plugins
            httpSender {
                uri = BuildConfig.ACRA_HTTP_URI
                basicAuthLogin = BuildConfig.ACRA_HTTP_USERNAME
                basicAuthPassword = BuildConfig.ACRA_HTTP_PASSWORD
                httpMethod = HttpSender.Method.POST
            }

            dialog {
                title = "Uncaught Exception"
//                text = ""
//                positiveButtonText = getString(R.string.dialog_positive)
//                negativeButtonText = getString(R.string.dialog_negative)
//                commentPrompt = getString(R.string.dialog_comment)
//                emailPrompt = getString(R.string.dialog_email)
//                resIcon = R.drawable.dialog_icon
//                resTheme = R.style.Theme_ArcaeaOffline
                reportDialogClass = CustomAcraCrashReportActivity::class.java
            }
        }
    }
}
