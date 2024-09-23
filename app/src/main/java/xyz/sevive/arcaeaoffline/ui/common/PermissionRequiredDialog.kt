package xyz.sevive.arcaeaoffline.ui.common

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flaky
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.components.preferences.BasePreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
fun rememberPermissionIcon(permission: String): ImageVector? {
    return remember(permission) {
        when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> Icons.Default.Storage
            Manifest.permission.POST_NOTIFICATIONS -> Icons.Default.NotificationsActive

            else -> null
        }
    }
}

@Composable
fun rememberPermissionDescription(permission: String): String {
    val context = LocalContext.current
    return remember(permission) {
        context.packageManager.getPermissionInfo(permission, 0).loadLabel(context.packageManager)
            .toString()
    }
}

@Composable
private fun PermissionTitle(permission: String, modifier: Modifier = Modifier) {
    val upperText = remember(permission) { permission.substringBeforeLast('.') }
    val lowerText = remember(permission) { permission.substringAfterLast('.') }

    Column(modifier) {
        Text(
            upperText,
            Modifier.secondaryItemAlpha(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            lineHeight = MaterialTheme.typography.labelSmall.fontSize,
        )
        Text(lowerText)
    }
}

@Composable
private fun PermissionWidget(permission: String, modifier: Modifier = Modifier) {
    val content = rememberPermissionDescription(permission)
    val icon = rememberPermissionIcon(permission)

    BasePreferencesWidget(
        title = { PermissionTitle(permission) },
        content = {
            Text(
                content,
                Modifier.secondaryItemAlpha(),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        leadingSlot = icon?.let { { Icon(it, contentDescription = null) } },
        trailingSlot = null,
        onClick = null,
        modifier = modifier,
    )
}

@Composable
fun PermissionRequiredDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    functionName: String? = null,
    permissions: List<String> = emptyList(),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { DialogConfirmButton(onClick = onConfirm) },
        modifier = modifier,
        dismissButton = { DialogDismissTextButton(onClick = onDismiss) },
        icon = { Icon(Icons.Default.Flaky, contentDescription = null) },
        title = { Text(stringResource(R.string.permission_required_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
                Text(
                    pluralStringResource(
                        R.plurals.permission_required_dialog_content,
                        count = permissions.size,
                        functionName.toString()
                    )
                )

                Column {
                    permissions.forEach { PermissionWidget(it) }
                }
            }
        },
    )
}

@Preview
@Composable
private fun PermissionRequiredDialogRealDevicePreview() {
    ArcaeaOfflineTheme {
        PermissionRequiredDialog(
            onDismiss = {},
            onConfirm = {},
            functionName = "Preview",
            permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            } else {
                listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            },
        )
    }
}
