package xyz.sevive.arcaeaoffline

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.opencv.android.OpenCVLoader
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

class MainActivity : ComponentActivity() {
    companion object {
        /** Extra carrying the route a fresh session should open on, e.g. an OCR sub-screen route. */
        const val EXTRA_INITIAL_ROUTE = "xyz.sevive.arcaeaoffline.extra.INITIAL_ROUTE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        OpenCVLoader.initLocal()
        FileKit.init(this)

        val initialRoute = intent?.getStringExtra(EXTRA_INITIAL_ROUTE)

        setContent {
            ArcaeaOfflineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen(initialNavigateRoute = initialRoute)
                }
            }
        }
    }
}
