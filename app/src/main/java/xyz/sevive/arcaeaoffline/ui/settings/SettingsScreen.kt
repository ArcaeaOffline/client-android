package xyz.sevive.arcaeaoffline.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.EmergencyModeActivity
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.IS_UNSTABLE_VERSION
import xyz.sevive.arcaeaoffline.ui.components.ActionButton

@Composable
internal fun settingsTitleActionCardShape(): CornerBasedShape {
    return RoundedCornerShape(
        topStart = MaterialTheme.shapes.medium.topStart,
        topEnd = MaterialTheme.shapes.medium.topEnd,
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp),
    )
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    val context = LocalContext.current

    Box(
        modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.page_padding))
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
            if (IS_UNSTABLE_VERSION) {
                item {
                    UnstableBuildAlert(Modifier.fillMaxWidth(), showDetails = true)
                }
            }

            item {
                ActionButton(
                    onClick = {
                        context.startActivity(Intent(context, EmergencyModeActivity::class.java))
                    },
                    title = stringResource(R.string.shortcut_emergency_long_label),
                    headSlot = {
                        Icon(
                            painterResource(R.drawable.ic_activity_emergency_mode),
                            contentDescription = null,
                        )
                    },
                    tailSlot = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                        )
                    },
                    buttonColors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                )
            }

            item {
                SettingsAbout()
            }
        }
    }
}
