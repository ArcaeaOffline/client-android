package xyz.sevive.arcaeaoffline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.apache.commons.io.IOUtils
import org.opencv.android.OpenCVLoader
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.ocr.ocrDigitsByContourKnn
import xyz.sevive.arcaeaoffline.ocr.rois.definition.DeviceAutoRoisT2
import xyz.sevive.arcaeaoffline.ocr.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.ocr.rois.masker.DeviceAutoRoisMaskerT2
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (OpenCVLoader.initDebug()) {
            Log.d("ocr", "OpenCV loaded")
        }
        setContent {
            ArcaeaOfflineTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Greeting("Android")
                        SelectFileButton(onClick = { showFileSelector() })
                    }
                }
            }
        }
    }

    private fun showFileSelector() {
        // Create an intent with the action to get content
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // Add the category for openable files
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // Set the type to any type (*/*)
        intent.type = "*/*"
        // Optionally, you can specify the MIME types you want to filter
        // For example, to select only PDF and TXT files, you can use
        // val mimeTypes = arrayOf("application/pdf", "text/plain")
        // intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        // Start the intent and wait for the result
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 3375)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check if the request code matches our file selector request
        if (requestCode == 3375) {
            // Check if the result is OK
            if (resultCode == RESULT_OK) {
                // Get the URI of the selected file
                val fileUri = data?.data // The URI with the location of the file
                // You can use the URI to get the file path, name, size, etc.
                // For example, to get the file name, you can use
                // val fileName = getFileName(fileUri)
                Log.i("file", fileUri.toString())
                if (fileUri != null) {
                    val inputStream =
                        contentResolver.openInputStream(fileUri) // get an input stream from the Uri

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

                    val roi = m.pure(e.pure)

                    val res = ocrDigitsByContourKnn(
                        roi, KNearest.load("/storage/emulated/0/Documents/Arcaea Offline/digits.denoised.knn.dat")
                    )

                    Log.i("result", res.toString())
                }
            }
        }
    }
}


@Composable
fun SelectFileButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = "Select...")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "text, wow", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArcaeaOfflineTheme {
        Greeting("Android")
    }
}