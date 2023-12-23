package xyz.sevive.arcaeaoffline.ui.common

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flaky
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import xyz.sevive.arcaeaoffline.R


private fun getPermissionIcon(permission: String): ImageVector? {
    return when (permission) {
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> Icons.Default.Save
        Manifest.permission.POST_NOTIFICATIONS -> Icons.Default.NotificationsActive

        else -> null
    }
}

private fun getPermissionLocalizedLabel(
    permission: String,
    pm: PackageManager,
): String {
    return pm.getPermissionInfo(permission, 0).loadLabel(pm).toString()
}

@Composable
private fun getPermissionDisplayAnnotatedString(permission: String): AnnotatedString {
    val permissionSplitBefore = permission.substringBeforeLast('.')
    val permissionSplitAfter = permission.substringAfterLast('.')

    return buildAnnotatedString {
        withStyle(SpanStyle(fontSize = 0.8.em)) {
            append("$permissionSplitBefore.")
        }
        append("\n$permissionSplitAfter")
    }
}

@Composable
fun PermissionRequiredDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    functionName: String? = null,
    permissions: Array<String> = arrayOf(),
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.general_ok))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.general_cancel))
            }
        },
        icon = { Icon(Icons.Default.Flaky, null) },
        title = { Text(stringResource(R.string.permission_required_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
                Text(
                    String.format(
                        pluralStringResource(
                            R.plurals.permission_required_dialog_content,
                            permissions.size,
                        ), functionName.toString()
                    )
                )

                for (permission in permissions) {
                    HorizontalDivider()

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_icon_text_padding)),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            val permissionIcon = getPermissionIcon(permission)
                            if (permissionIcon != null) Icon(permissionIcon, null)

                            Text(getPermissionDisplayAnnotatedString(permission))
                        }

                        Text(
                            getPermissionLocalizedLabel(permission, context.packageManager),
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        },
    )
}
