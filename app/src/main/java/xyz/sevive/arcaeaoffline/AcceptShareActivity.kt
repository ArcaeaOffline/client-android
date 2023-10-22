package xyz.sevive.arcaeaoffline

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.apache.commons.io.IOUtils
import org.opencv.android.OpenCVLoader
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.ocr.DeviceOcr
import xyz.sevive.arcaeaoffline.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceAutoRoisMaskerT2
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceAutoRoisT2
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.settings.SettingsOcr
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScore
import xyz.sevive.arcaeaoffline.ui.components.ScoreCard
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptShareContent(arcaeaScore: ArcaeaScore? = null, modifier: Modifier = Modifier) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = stringResource(R.string.title_activity_accept_share)) },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (arcaeaScore != null) {
                ScoreCard(title = "Testing", ratingClass = 0, arcaeaScore = arcaeaScore)
            } else {
                Text("Waiting for result...")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AcceptSharContentPreview(modifier: Modifier = Modifier) {
    ArcaeaOfflineTheme {
        AcceptShareContent(
            ArcaeaScore(
                9562389, 2833, 37, 75, null,
                3375, 0, 1, "OCR",
            ),
            modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AcceptSharContentNoScorePreview(modifier: Modifier = Modifier) {
    ArcaeaOfflineTheme {
        AcceptShareContent(null, modifier)
    }
}


class AcceptShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (OpenCVLoader.initDebug()) {
            Log.d("ocr", "OpenCV loaded")
        }

        setTitle(R.string.title_activity_accept_share)
        setContent {
            AcceptShareContent(arcaeaScore = null)
        }

        // https://www.cnblogs.com/daner1257/p/5581443.html
        val action = intent.action
        val type = intent.type
        if (action.equals(Intent.ACTION_SEND) && type != null && type.startsWith("image/")) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            //接收多张图片: ArrayList<Uri> uris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                try {
                    val inputStream =
                        contentResolver.openInputStream(uri) // get an input stream from the Uri

                    val bytes: ByteArray =
                        IOUtils.toByteArray(inputStream) // convert the input stream to a byte array

                    val image = Imgcodecs.imdecode(
                        MatOfByte(*bytes), Imgcodecs.IMREAD_UNCHANGED
                    ) // decode the byte array to a Mat object using OpenCV

                    val rois = DeviceAutoRoisT2(
                        image.size().width.toInt(), image.size().height.toInt()
                    )

                    val e = DeviceRoisExtractor(rois, image)
                    val m = DeviceAutoRoisMaskerT2()

                    val knnModel = KNearest.load(
                        SettingsOcr(this.baseContext).knnModelFile().path
                    )
                    val phashDb =
                        ImagePhashDatabase(SettingsOcr(this.applicationContext).pHashDatabaseFile().path)

                    val ocr = DeviceOcr(e, m, knnModel, phashDb)

                    val ocrResult = ocr.ocr()

                    val ocrScore = ArcaeaScore(
                        ocrResult.score, ocrResult.pure, ocrResult.far, ocrResult.lost,
                        null, ocrResult.maxRecall, 0, 1, "OCR WIP"
                    )

                    setContent {
                        AcceptShareContent(arcaeaScore = ocrScore)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this.applicationContext, e.toString(), Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Share OCR Result"  // getString(R.string.channel_name)
            val descriptionText = "This is description"  // getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("SHARE_OCR_RESULT", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
