package xyz.sevive.arcaeaoffline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import xyz.sevive.arcaeaoffline.ui.screens.UnstableVersionAlertScreen
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

class MainActivity : ComponentActivity() {
    private val _unstableAlertReadState = MutableStateFlow<Boolean?>(null)
    private val unstableAlertReadState = _unstableAlertReadState.asStateFlow()

    private val unstableFlavorPreferencesRepository by lazy {
        (this.application as ArcaeaOfflineApplication).dataStoreRepositoryContainer.unstableFlavorPreferences
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OpenCVLoader.initLocal()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val unstableAlertRead by unstableAlertReadState.collectAsStateWithLifecycle()

            ArcaeaOfflineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AnimatedContent(
                        targetState = unstableAlertRead,
                        transitionSpec = {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(
                                slideOutHorizontally { -it } + fadeOut()).using(
                                // Disable clipping since the faded slide-in/out should
                                // be displayed out of bounds.
                                SizeTransform(clip = false)
                            )
                        },
                        label = "unstableVersionScreenSlideOut",
                    ) {
                        when (it) {
                            true -> MainScreen()
                            false -> UnstableVersionAlertScreen(
                                onConfirm = { confirmUnstableAlertRead() },
                                onDeny = { finishAffinity() },
                                windowSizeClass,
                            )

                            null -> Box(Modifier.fillMaxHeight(), Alignment.Center) {
                                Column {
                                    Text("Initializing")
                                    LinearProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            unstableFlavorPreferencesRepository.preferencesFlow.collect {
                _unstableAlertReadState.value = it.unstableAlertRead
            }
        }
    }

    private fun confirmUnstableAlertRead() {
        lifecycleScope.launch { unstableFlavorPreferencesRepository.setAlertRead(true) }
    }
}
