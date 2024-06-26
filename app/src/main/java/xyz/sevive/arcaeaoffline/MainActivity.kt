package xyz.sevive.arcaeaoffline

import android.content.Context
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import xyz.sevive.arcaeaoffline.helpers.GlobalOcrDependencyHelper
import xyz.sevive.arcaeaoffline.ui.screens.UnstableVersionAlertScreen
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

val Context.unstableDataStore: DataStore<Preferences> by preferencesDataStore(name = "unstable")

val UNSTABLE_ALERT_READ = booleanPreferencesKey("unstable_alert_read")

class MainActivity : ComponentActivity() {
    private val _unstableAlertReadState = MutableStateFlow<Boolean?>(null)
    private val unstableAlertReadState = _unstableAlertReadState.asStateFlow()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.Default).launch {
            _unstableAlertReadState.value = baseContext.unstableDataStore.data.map { preferences ->
                preferences[UNSTABLE_ALERT_READ] ?: false
            }.first()
        }

        OpenCVLoader.initLocal()

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val coroutineScope = rememberCoroutineScope()

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
                                onConfirm = {
                                    coroutineScope.launch {
                                        confirmUnstableAlertRead()
                                    }
                                },
                                onDeny = { finishAffinity() },
                                windowSizeClass,
                            )

                            null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(Modifier.weight(1f))

                                LinearProgressIndicator()
                                Text("Initializing")

                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        GlobalOcrDependencyHelper.loadAll(this)
    }

    private suspend fun confirmUnstableAlertRead() {
        this.baseContext.unstableDataStore.edit { settings ->
            settings[UNSTABLE_ALERT_READ] = true
            _unstableAlertReadState.value = true
        }
    }
}
