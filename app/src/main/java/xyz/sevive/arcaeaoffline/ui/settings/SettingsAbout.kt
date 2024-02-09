package xyz.sevive.arcaeaoffline.ui.settings

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.TypedValueCompat.pxToDp
import kotlinx.coroutines.delay
import xyz.sevive.arcaeaoffline.BuildConfig
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.constants.IS_UNSTABLE_VERSION
import xyz.sevive.arcaeaoffline.ui.components.ActionButton
import xyz.sevive.arcaeaoffline.ui.components.TextWithLabel
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard


@Composable
fun AppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Image(
        context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID).toBitmap()
            .asImageBitmap(),
        null,
        modifier = modifier,
    )

}

class SettingsAboutException : Exception() {
    override val message: String? get() = null
}

@SuppressLint("ShowToast")
@Composable
fun SettingsAbout() {
    val context = LocalContext.current

    var crashCounter by rememberSaveable { mutableIntStateOf(7) }
    var crashCounterLastClickTimestamp by rememberSaveable { mutableStateOf<Long?>(null) }
    var toast: Toast? by remember { mutableStateOf(null) }

    if (crashCounter <= 0) {
        if (toast != null) toast!!.cancel()
        Toast.makeText(context, "😭", Toast.LENGTH_SHORT).show()
        throw SettingsAboutException()
    }

    LaunchedEffect(crashCounterLastClickTimestamp) {
        if (crashCounterLastClickTimestamp != null) {
            delay(2000)
            Log.d("SettingsAbout", "Resetting custom crash counter")
            crashCounter = 7
        }
    }

    TitleOutlinedCard(title = {
        ActionButton(
            onClick = {
                if (crashCounter > 0) {
                    crashCounter -= 1
                    crashCounterLastClickTimestamp = System.currentTimeMillis()
                }

                if (crashCounter in 1..4) {
                    if (toast != null) {
                        toast!!.cancel()
                    }
                    toast = Toast.makeText(context, crashCounter.toString(), Toast.LENGTH_SHORT)
                    toast!!.show()
                }
            },
            title = stringResource(R.string.settings_about_title),
            shape = settingsTitleActionCardShape(),
            buttonColors = if (IS_UNSTABLE_VERSION) ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
            ) else null,
            headSlot = { Icon(Icons.Default.Info, null) },
        )
    }) { padding ->
        Column(
            Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val titleTextStyle = MaterialTheme.typography.titleLarge
                val iconDp = with(LocalDensity.current) {
                    pxToDp(
                        titleTextStyle.fontSize.toPx() * 2.0.toFloat(),
                        LocalContext.current.resources.displayMetrics
                    )
                }.dp
                AppIcon(modifier = Modifier.size(iconDp))
                Text(
                    stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            if (IS_UNSTABLE_VERSION) {
                UnstableBuildAlert(Modifier.fillMaxWidth(), showDetails = false)
            }

            TextWithLabel(
                text = BuildConfig.APPLICATION_ID,
                label = stringResource(R.string.settings_about_application_id)
            )
            TextWithLabel(
                text = BuildConfig.VERSION_NAME,
                label = stringResource(R.string.settings_about_version)
            )
            TextWithLabel(
                text = BuildConfig.VERSION_CODE.toString(),
                label = stringResource(R.string.settings_about_version_code)
            )
        }
    }
}
