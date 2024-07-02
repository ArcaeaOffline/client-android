package xyz.sevive.arcaeaoffline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.documentfile.provider.DocumentFile
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityUi
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


class EmergencyModeActivity : ComponentActivity() {
    private val viewModel by viewModels<EmergencyModeActivityViewModel> { AppViewModelProvider.Factory }

    private val setOutputDirectoryRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) {
                Toast.makeText(this, "Cancel, resultCode ${it.resultCode}", Toast.LENGTH_SHORT)
                return@registerForActivityResult
            }

            val uri = it.data?.data
            if (uri == null) {
                Toast.makeText(this, "Cancel, uri is null", Toast.LENGTH_SHORT)
                return@registerForActivityResult
            }

            setOutputDirectory(uri)
        }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.app_name)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            ArcaeaOfflineTheme {
                EmergencyModeActivityUi(
                    onSelectOutputDirectory = {
                        setOutputDirectoryRequest.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                    },
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun setOutputDirectory(uri: Uri) {
        DocumentFile.fromTreeUri(this, uri)?.let {
            val permissionFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            this.contentResolver.takePersistableUriPermission(uri, permissionFlags)

            viewModel.setOutputDirectory(it)
        }
    }
}
