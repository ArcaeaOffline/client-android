package xyz.sevive.arcaeaoffline.ui.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget


@Composable
fun NavEntryNavigateButton(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextPreferencesWidget(
        title = title,
        content = description,
        leadingIcon = icon,
        trailingIcon = Icons.AutoMirrored.Default.ArrowRight,
        onClick = onClick,
    )
}

@Composable
fun NavEntryNavigateButton(
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    NavEntryNavigateButton(
        title = stringResource(titleResId),
        description = descriptionResId?.let { stringResource(it) },
        icon = icon,
        onClick = onClick,
    )
}
