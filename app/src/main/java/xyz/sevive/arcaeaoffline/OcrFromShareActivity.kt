package xyz.sevive.arcaeaoffline

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.content.IntentCompat
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.opencv.android.OpenCVLoader
import xyz.sevive.arcaeaoffline.helpers.activity.getSourcePackageName
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareScreen
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

class OcrFromShareActivity : ComponentActivity() {
    private val viewModel by viewModels<OcrFromShareViewModel>(factoryProducer = { AppViewModelProvider.Factory })

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        OpenCVLoader.initLocal()
        FileKit.init(this)

        setTitle(R.string.title_activity_ocr_from_share)

        viewModel.setShareSourceApp(this.getSourcePackageName(), packageManager)
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
                    viewModel = viewModel,
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
        val uri =
            IntentCompat.getParcelableExtra(
                intent,
                Intent.EXTRA_STREAM,
                Uri::class.java,
            ) ?: return

        // get an input stream from the Uri
        // TODO: use input stream
        val imageBytes =
            runCatching {
                runBlocking { PlatformFile(uri).readBytes() }
            }.getOrElse {
                viewModel.setException(Exception("Error reading image", it))
                return
            }

        val imgBitmap = BitmapFactory.decodeStream(imageBytes.inputStream())
        viewModel.setBitmap(imgBitmap)
        runBlocking {
            launch {
                viewModel.startOcr(uri, this@OcrFromShareActivity)
            }
        }
    }
}
