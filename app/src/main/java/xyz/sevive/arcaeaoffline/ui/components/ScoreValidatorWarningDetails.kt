package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.helpers.ArcaeaScoreValidatorWarning

@Composable
internal fun ScoreValidatorWarningDetailItem(warning: ArcaeaScoreValidatorWarning) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandArrowRotateDegree by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "expandArrow"
    )

    val context = LocalContext.current
    val cardPadding = dimensionResource(R.dimen.general_card_padding)

    val title = warning.getTitle(context)
    val message = warning.getMessage(context)

    val detailsEnabled = message != null

    TitleOutlinedCard(title = {
        Row(
            Modifier
                .clickable { if (detailsEnabled) expanded = !expanded }
                .padding(it),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.padding(end = dimensionResource(R.dimen.general_icon_text_padding))
            )

            Column {
                Text(title)
                Text(
                    warning.id,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light)
                )
            }

            if (detailsEnabled) {
                Spacer(Modifier.weight(1f))

                Icon(Icons.Default.ExpandMore,
                    contentDescription = null,
                    Modifier.graphicsLayer { rotationZ = expandArrowRotateDegree })
            }
        }
    }) {
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(cardPadding)) {
                Text(message!!)
            }
        }
    }
}

@Composable
fun ScoreValidatorWarningDetails(
    warnings: List<ArcaeaScoreValidatorWarning>, modifier: Modifier = Modifier
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))
    ) {
        for (warning in warnings) {
            ScoreValidatorWarningDetailItem(warning)
        }
    }
}
