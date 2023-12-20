package xyz.sevive.arcaeaoffline

import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.IntentCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


class OcrFromShareActivity : ComponentActivity() {
    private lateinit var ocrDependencyViewModel: OcrDependencyViewModel
    private lateinit var ocrFromShareViewModel: OcrFromShareViewModel

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ocrDependencyViewModel = ViewModelProvider(this)[OcrDependencyViewModel::class.java]
        ocrFromShareViewModel = ViewModelProvider(
            this,
            factory = AppViewModelProvider.Factory,
        )[OcrFromShareViewModel::class.java]

        setTitle(R.string.title_activity_ocr_from_share)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            ArcaeaOfflineTheme {
                OcrFromShareScreen(
                    windowSizeClass,
                    getShareAppName(),
                    getShareAppIcon(),
                    onReturnToShareApp = { finishAffinity() },
                    onStayInApp = {
                        val intent = Intent(this@OcrFromShareActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finishAffinity()
                    },
                    ocrDependencyViewModel = ocrDependencyViewModel,
                    ocrFromShareViewModel = ocrFromShareViewModel
                )
            }
        }

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("image/") == true) {
                    handleSingleImageOcr()
                }
            }

            // TODO: jump to ocr page
//            Intent.ACTION_SEND_MULTIPLE -> {
//                if (intent.type?.startsWith("image/") == true) {
//
//                }
//            }

            else -> {}
        }
    }

    private fun getShareAppPackageName(): String? {
        val fromIntent = intent.`package`

        if (fromIntent != null) {
            return fromIntent
        }

        // intent.package no value, try from referrer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && referrer != null) {
            return referrer.toString().replace("android-app://", "")
        }

        return null
    }

    private fun getShareAppName(): String? {
        var shareAppName: String? = null

        val packageName = getShareAppPackageName()

        if (packageName != null) {
            shareAppName = try {
                val info = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(info).toString()
            } catch (e: NameNotFoundException) {
                packageName
            }
        }

        return shareAppName
    }

    private fun getShareAppIcon(): ImageBitmap? {
        val packageName = getShareAppPackageName() ?: return null

        return try {
            packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
        } catch (e: NameNotFoundException) {
            null
        }
    }


    private fun handleSingleImageOcr() {
        // fix 'getParcelableExtra(String!): T?' is deprecated. Deprecated in Java
        // https://stackoverflow.com/a/75824124/16484891
        // CC BY-SA 4.0
        val uri = IntentCompat.getParcelableExtra(
            intent, Intent.EXTRA_STREAM, Uri::class.java
        ) ?: return

        // get an input stream from the Uri
        val inputStream = contentResolver.openInputStream(uri)

        if (inputStream == null) {
            ocrFromShareViewModel.setException(Exception("Error reading image"))
            return
        }

        val inputStreamRead = IOUtils.toByteArray(inputStream)

        val imgBitmap = BitmapFactory.decodeStream(inputStreamRead.inputStream())
        ocrFromShareViewModel.setImageBitmap(imgBitmap)
        ocrFromShareViewModel.startOcr(uri, this)
    }
}
