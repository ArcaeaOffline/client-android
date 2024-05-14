package xyz.sevive.arcaeaoffline.ui.database.b30list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jakewharton.threetenabp.AndroidThreeTen
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ScoreBest
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreen
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DatabaseB30ListScreen(
    onNavigateUp: () -> Unit,
    databaseB30ListViewModel: DatabaseB30ListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    var showOptions by rememberSaveable { mutableStateOf(false) }

    val uiItems by databaseB30ListViewModel.uiItems.collectAsState()
    val limit by databaseB30ListViewModel.limit.collectAsState()

    SubScreenContainer(topBar = {
        SubScreenTopAppBar(
            onNavigateUp = onNavigateUp,
            title = { Text(stringResource(DatabaseScreen.B30.title)) },
            actions = {
                IconButton(onClick = { showOptions = true }) {
                    Icon(Icons.Default.Settings, null)
                }
            },
        )
    }) {
        if (uiItems.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
                items(uiItems, key = { it.id }) {
                    Box(Modifier.animateItemPlacement()) {
                        DatabaseB30ListItem(it)
                    }
                }
            }
        }
    }

    if (showOptions) {
        AlertDialog(onDismissRequest = { showOptions = false },
            confirmButton = {},
            title = { Text(limit.toString()) },
            text = {
                Slider(
                    value = limit.toFloat(),
                    onValueChange = { databaseB30ListViewModel.setLimit(it.toInt()) },
                    valueRange = 10.0f..60.0f,
                    steps = 4,
                )
            })
    }
}

@Preview
@Composable
fun DatabaseB30ListItemPreview() {
    ArcaeaOfflineTheme {
        AndroidThreeTen.init(LocalContext.current)

        DatabaseB30ListItem(
            DatabaseB30ListUiItem(
                index = 5,
                scoreBest = ScoreBest(
                    id = 1,
                    songId = "test",
                    ratingClass = ArcaeaScoreRatingClass.FUTURE,
                    score = 99500000,
                    pure = null,
                    shinyPure = null,
                    far = null,
                    lost = null,
                    date = 0,
                    maxRecall = null,
                    modifier = null,
                    clearType = null,
                    potential = 12.00,
                    comment = null,
                ),
                chart = null,
            )
        )
    }
}
