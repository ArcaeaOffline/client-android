package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


object ListGroupHeaderDefaults {
    val paddingValues = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    val contentColor
        @Composable get() = MaterialTheme.colorScheme.primary
    val textStyle
        @Composable get() = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
}

@Composable
fun ListGroupHeader(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = ListGroupHeaderDefaults.paddingValues,
    contentColor: Color = ListGroupHeaderDefaults.contentColor,
    textStyle: TextStyle = ListGroupHeaderDefaults.textStyle,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.padding(paddingValues)) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides textStyle,
        ) {
            content()
        }
    }
}

@Composable
fun ListGroupHeader(
    text: String,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = ListGroupHeaderDefaults.paddingValues,
    contentColor: Color = ListGroupHeaderDefaults.contentColor,
    textStyle: TextStyle = ListGroupHeaderDefaults.textStyle,
) {
    ListGroupHeader(
        modifier = modifier,
        paddingValues = paddingValues,
        contentColor = contentColor,
        textStyle = textStyle,
    ) {
        Text(text = text)
    }
}
