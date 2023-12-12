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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ActionCard
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreens


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
    ) {
        LazyColumn(
            Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.main_screen_list_arrangement_spaced_by)),
        ) {
            item {
                DatabaseStatus(Modifier.fillMaxWidth())
            }

            item {
                ActionCard(
                    onClick = { onNavigateToSubRoute(DatabaseScreens.Manage.route) },
                    title = stringResource(DatabaseScreens.Manage.title),
                    headSlot = {
                        Icon(Icons.Default.Build, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }

            item {
                ActionCard(
                    onClick = { onNavigateToSubRoute(DatabaseScreens.AddScore.route) },
                    title = stringResource(DatabaseScreens.AddScore.title),
                    headSlot = {
                        Icon(Icons.Default.Add, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }

            item {
                ActionCard(
                    onClick = { onNavigateToSubRoute(DatabaseScreens.ScoreList.route) },
                    title = stringResource(DatabaseScreens.ScoreList.title),
                    headSlot = {
                        Icon(Icons.AutoMirrored.Default.List, null)
                    },
                    tailSlot = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    },
                )
            }
        }
    }
}
