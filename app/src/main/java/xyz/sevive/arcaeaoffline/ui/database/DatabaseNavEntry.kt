package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ActionButton
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations


@Composable
fun DatabaseNavEntry(
    onNavigateToSubRoute: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            Text(
                stringResource(R.string.nav_database),
                Modifier.padding(0.dp, dimensionResource(R.dimen.general_page_padding)),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        containerColor = Color.Transparent,
    ) {
        LazyColumn(
            Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.main_screen_list_arrangement_spaced_by)),
        ) {
            item {
                DatabaseStatus(Modifier.fillMaxWidth())
            }

            item {
                ActionButton(
                    onClick = { onNavigateToSubRoute(DatabaseScreenDestinations.Manage.route) },
                    title = stringResource(DatabaseScreenDestinations.Manage.title),
                    headSlot = {
                        Icon(Icons.Default.Build, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }

            item {
                ActionButton(
                    onClick = { onNavigateToSubRoute(DatabaseScreenDestinations.AddScore.route) },
                    title = stringResource(DatabaseScreenDestinations.AddScore.title),
                    headSlot = {
                        Icon(Icons.Default.Add, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }

            item {
                ActionButton(
                    onClick = { onNavigateToSubRoute(DatabaseScreenDestinations.ScoreList.route) },
                    title = stringResource(DatabaseScreenDestinations.ScoreList.title),
                    headSlot = {
                        Icon(Icons.AutoMirrored.Default.List, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }

            item {
                ActionButton(
                    onClick = { onNavigateToSubRoute(DatabaseScreenDestinations.B30.route) },
                    title = stringResource(DatabaseScreenDestinations.B30.title),
                    headSlot = {
                        Icon(Icons.Default.Star, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }
        }
    }
}
