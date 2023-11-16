package xyz.sevive.arcaeaoffline.ui.settings

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import xyz.sevive.arcaeaoffline.BuildConfig
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ActionCard
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard
import xyz.sevive.arcaeaoffline.ui.constants.IS_UNSTABLE_VERSION


@Composable
internal fun SettingsAboutDescLabel(
    label: String, content: @Composable () -> Unit
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        content()
    }
}

internal fun drawableToBitmap(drawable: Drawable): Bitmap? {
    // https://stackoverflow.com/a/24389104/16484891
    // CC BY-SA 4.0
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

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

@Composable
fun SettingsAboutCard() {
    TitleOutlinedCard(title = {
        ActionCard(onClick = { },
            title = stringResource(R.string.settings_about_title),
            shape = settingsTitleActionCardShape(),
            cardColors = if (IS_UNSTABLE_VERSION) CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
            ) else null,
            headSlot = { Icon(Icons.Default.Info, null) })
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

            SettingsAboutDescLabel(stringResource(R.string.settings_about_application_id)) {
                Text(BuildConfig.APPLICATION_ID)
            }
            SettingsAboutDescLabel(stringResource(R.string.settings_about_version)) {
                Text(BuildConfig.VERSION_NAME)
            }
            SettingsAboutDescLabel(stringResource(R.string.settings_about_version_code)) {
                Text(BuildConfig.VERSION_CODE.toString())
            }
        }
    }
}
