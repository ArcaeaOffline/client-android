package xyz.sevive.arcaeaoffline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.IntentCompat
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.opencv.android.OpenCVLoader
import xyz.sevive.arcaeaoffline.helpers.activity.getSourcePackageName
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareScreen
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

class OcrFromShareActivity : ComponentActivity() {
    private val viewModel by viewModel<OcrFromShareViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        OpenCVLoader.initLocal()
        FileKit.init(this)

        setTitle(R.string.title_activity_ocr_from_share)

        viewModel.setShareSourceApp(getSourcePackageName(), packageManager)
        setContent {
            ArcaeaOfflineTheme {
                OcrFromShareScreen(
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
                    // fix 'getParcelableExtra(String!): T?' is deprecated. Deprecated in Java
                    // https://stackoverflow.com/a/75824124/16484891
                    // CC BY-SA 4.0
                    IntentCompat
                        .getParcelableExtra(
                            intent,
                            Intent.EXTRA_STREAM,
                            Uri::class.java,
                        )?.let(viewModel::onImageShared)
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
}
