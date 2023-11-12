package xyz.sevive.arcaeaoffline.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@SuppressLint("ModifierParameter")
@Composable
fun ActionCard(
    onClick: () -> Unit,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    cardColors: CardColors? = null,
    headSlot: @Composable () -> Unit = {},
    tailSlot: @Composable () -> Unit = {}
) {
    val _cardColors = cardColors ?: CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer)
    )

    Card(modifier, shape = shape, colors = _cardColors) {
        Row(
            Modifier
                .clickable { onClick() }
                .padding(dimensionResource(R.dimen.action_card_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            headSlot()

            Spacer(modifier.width(dimensionResource(R.dimen.action_card_icon_text_padding)))

            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(modifier.width(dimensionResource(R.dimen.action_card_icon_text_padding)))

            tailSlot()

            Spacer(modifier.width(dimensionResource(R.dimen.action_card_icon_text_padding)))
        }
    }
}

@Preview
@Composable
fun ActionCardPreview() {
    ArcaeaOfflineTheme {
        Column(
            Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionCard({}, "Test Card")
            ActionCard({}, "Test Card w/ desc", "wow description")
            ActionCard(onClick = {},
                title = "Test Card w/ icon",
                description = "wow an arrow",
                tailSlot = {
                    Icon(Icons.Default.ArrowForward, null)
                })
            ActionCard(onClick = {},
                title = "Test Card w/ two icons",
                description = "wow icons",
                headSlot = {
                    Icon(Icons.Default.UploadFile, null)
                },
                tailSlot = {
                    Icon(Icons.Default.ChevronRight, null)
                })
        }
    }
}
