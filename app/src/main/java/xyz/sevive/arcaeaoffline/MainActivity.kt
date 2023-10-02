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
import org.opencv.android.OpenCVLoader
import xyz.sevive.arcaeaoffline.settings.SettingsOcr
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

class MainActivity : ComponentActivity() {
    val KNN_MODEL_FILE_SELECT = 3375

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
        startActivityForResult(
            Intent.createChooser(intent, "Select a file"),
            this.KNN_MODEL_FILE_SELECT
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.KNN_MODEL_FILE_SELECT) {
            if (resultCode == RESULT_OK) {
                val fileUri = data?.data
                if (fileUri != null) {
                    SettingsOcr(this.baseContext).importKnn(fileUri)
                }
            }
        }
    }
}


@Composable
fun SelectFileButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = "Select KNN...")
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