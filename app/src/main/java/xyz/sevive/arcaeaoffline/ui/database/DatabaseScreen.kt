package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.components.ActionCard
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreens


@Composable
fun DatabaseStatusComponent(
    databaseEntryViewModel: DatabaseEntryViewModel, modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val propertyVersion by databaseEntryViewModel.propertyVersion.collectAsState()
    val packList by databaseEntryViewModel.packList.collectAsState()
    val songList by databaseEntryViewModel.songList.collectAsState()
    val difficultyList by databaseEntryViewModel.difficultyList.collectAsState()

    if (propertyVersion == null) {
        Row {
            Text("Database not initialized", modifier.weight(1f))
            Button(onClick = { coroutineScope.launch { databaseEntryViewModel.initDatabase() } }) {
                Text("Init")
            }
        }
    } else {
        Row {
            Text(
                "Database ${packList.size} packs, ${songList.size} songs, ${difficultyList.size} difficulties."
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseEntryScreen(
    navigateToSubRoute: (route: String) -> Unit,
    modifier: Modifier = Modifier,
    databaseEntryViewModel: DatabaseEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Surface(modifier.fillMaxSize()) {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(
                    stringResource(R.string.nav_database),
                    style = MaterialTheme.typography.titleLarge
                )
            })
        }) { padding ->
            LazyColumn(modifier.padding(padding)) {
                item {
                    DatabaseStatusComponent(databaseEntryViewModel = databaseEntryViewModel)
                }

                item {
                    ActionCard(
                        onClick = { navigateToSubRoute(DatabaseScreens.Manage.route) },
                        title = "Manage"
                    ) {
                        Icon(Icons.Default.ArrowForward, null)
                    }
                }
            }
        }
    }
}

