package xyz.sevive.arcaeaoffline

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.IOUtils
import org.opencv.android.OpenCVLoader
import xyz.sevive.arcaeaoffline.helpers.activity.getSourcePackageName
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareScreen
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


class OcrFromShareActivity : ComponentActivity() {
    private lateinit var ocrDependencyViewModel: OcrDependencyViewModel
    private lateinit var ocrFromShareViewModel: OcrFromShareViewModel

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OpenCVLoader.initLocal()

        ocrDependencyViewModel = ViewModelProvider(this)[OcrDependencyViewModel::class.java]
        ocrFromShareViewModel = ViewModelProvider(
            this,
            factory = AppViewModelProvider.Factory,
        )[OcrFromShareViewModel::class.java]

        setTitle(R.string.title_activity_ocr_from_share)

        ocrFromShareViewModel.setShareSourceApp(this.getSourcePackageName(), packageManager)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            ArcaeaOfflineTheme {
                OcrFromShareScreen(
                    windowSizeClass,
                    onReturnToShareApp = { finishAffinity() },
                    onStayInApp = {
                        val intent = Intent(this@OcrFromShareActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finishAffinity()
                    },
                    ocrDependencyViewModel = ocrDependencyViewModel,
                    ocrFromShareViewModel = ocrFromShareViewModel,
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
        ocrFromShareViewModel.setBitmap(imgBitmap)
        runBlocking {
            launch {
                ocrFromShareViewModel.startOcr(uri, this@OcrFromShareActivity)
            }
        }
    }
}
