package xyz.sevive.arcaeaoffline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import org.apache.commons.io.IOUtils
import org.opencv.android.OpenCVLoader
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.database.entities.Score
import xyz.sevive.arcaeaoffline.ocr.DeviceOcr
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceAutoRoisMaskerT2
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceAutoRoisT2
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.ui.components.ScoreCard
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel
import xyz.sevive.arcaeaoffline.ui.models.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrFromShareContent(
    modifier: Modifier = Modifier,
    ocrDependencyViewModel: OcrDependencyViewModel = viewModel(),
    ocrFromShareViewModel: OcrFromShareViewModel = viewModel(),
) {
    val uiState = ocrFromShareViewModel.uiState.collectAsState()
    val score = uiState.value.score
    val scoreError = uiState.value.error

    ArcaeaOfflineTheme {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_activity_ocr_from_share)) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(modifier) {
                    OcrDependencyKnnModelStatus(
                        state = ocrDependencyViewModel.knnModelState.collectAsState()
                    )
                    OcrDependencyPhashDatabaseStatus(
                        state = ocrDependencyViewModel.phashDatabaseState.collectAsState()
                    )
                }

                if (score != null) {
                    ScoreCard(score = score)
                } else if (scoreError != null) {
                    Text(
                        scoreError.message ?: scoreError.toString(),
                        modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Waiting for result...", modifier.padding(8.dp))
                }
            }
        }
    }
}

class OcrFromShareActivity : ComponentActivity() {
    private lateinit var ocrDependencyViewModel: OcrDependencyViewModel
    private lateinit var ocrFromShareViewModel: OcrFromShareViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenCVLoader.initDebug()

        ocrDependencyViewModel = ViewModelProvider(this)[OcrDependencyViewModel::class.java]
        ocrFromShareViewModel = ViewModelProvider(this)[OcrFromShareViewModel::class.java]

        setTitle(R.string.title_activity_ocr_from_share)
        setContent {
            OcrFromShareContent(
                ocrDependencyViewModel = ocrDependencyViewModel,
                ocrFromShareViewModel = ocrFromShareViewModel
            )
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

                    val ocrDependencyPaths = OcrDependencyPaths(this.applicationContext)
                    ocrDependencyViewModel.loadKnnModel(ocrDependencyPaths.knnModelFile)
                    ocrDependencyViewModel.loadPhashDatabase(ocrDependencyPaths.phashDatabaseFile)

                    val knnModel = ocrDependencyViewModel.knnModelState.value.model
                    val phashDb = ocrDependencyViewModel.phashDatabaseState.value.db
                    if (knnModel == null || phashDb == null) {
                        throw Exception("OCR dependency missing, cannot continue")
                    }

                    val ocr = DeviceOcr(e, m, knnModel, phashDb)
                    val ocrResult = ocr.ocr()

                    val ocrScore = Score(
                        0,
                        ocrResult.songId ?: "",
                        ocrResult.ratingClass,
                        ocrResult.score,
                        ocrResult.pure,
                        ocrResult.far,
                        ocrResult.lost,
                        null,
                        ocrResult.maxRecall,
                        0,
                        1,
                        "OCR WIP"
                    )

                    ocrFromShareViewModel.setResult(ocrScore)
                } catch (e: Exception) {
                    ocrFromShareViewModel.setResult(null, e)
                }
            }
        }
    }
}
