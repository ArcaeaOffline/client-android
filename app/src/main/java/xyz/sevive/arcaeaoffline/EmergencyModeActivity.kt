package xyz.sevive.arcaeaoffline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityUi
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

class EmergencyModeActivity : ComponentActivity() {
    private val viewModel by viewModels<EmergencyModeActivityViewModel> { AppViewModelProvider.Factory }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setTitle(R.string.app_name)

        FileKit.init(this)

        setContent {
            ArcaeaOfflineTheme {
                EmergencyModeActivityUi(
                    viewModel = viewModel,
                )
            }
        }
    }
}
