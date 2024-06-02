package xyz.sevive.arcaeaoffline.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R


@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.general_no_results),
) {
    Box(modifier) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                Modifier
                    .size(50.dp)
                    .alpha(0.5f)
            )
            Text(text, style = MaterialTheme.typography.titleLarge)
        }
    }
}
