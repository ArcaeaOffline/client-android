package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.ui.components.SelectItem
import xyz.sevive.arcaeaoffline.ui.components.SelectOption
import xyz.sevive.arcaeaoffline.ui.components.SettingsGroupHeader
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseSubScreen
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreen
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseNavEntry(
    modifier: Modifier = Modifier,
    vm: DatabaseNavEntryViewModel = koinViewModel(),
) {
    val navContext = LocalListDetailNavigationContext.current
    val playResultCount by vm.playResultCount.collectAsStateWithLifecycle()
    val scoringMode by vm.scoringMode.collectAsStateWithLifecycle()

    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreen.Database.title)) })
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues),
        ) {
            SettingsGroupHeader(stringResource(R.string.database_manage_scoring_mode_title))

            SelectItem(
                options =
                    listOf(
                        SelectOption(
                            value = ArcaeaScoringMode.B30_R10,
                            label = stringResource(R.string.database_manage_scoring_mode_b30_r10),
                            description = ArcaeaScoringMode.B30_R10.key.toString(),
                        ),
                        SelectOption(
                            value = ArcaeaScoringMode.B50,
                            label = stringResource(R.string.database_manage_scoring_mode_b50),
                            description = ArcaeaScoringMode.B50.key.toString(),
                        ),
                    ),
                selected = scoringMode,
                onSelect = vm::setScoringMode,
                Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            NavEntryNavigateButton(
                title = stringResource(DatabaseSubScreen.ScoreList.title),
                description =
                    pluralStringResource(
                        R.plurals.database_play_result_entries,
                        playResultCount,
                        playResultCount,
                    ),
                icon = Icons.AutoMirrored.Default.List,
            ) {
                navContext.navigateToDetail(DatabaseSubScreen.ScoreList.route)
            }

            NavEntryNavigateButton(
                titleResId = DatabaseSubScreen.AddPlayResult.title,
                icon = Icons.Default.Add,
            ) {
                navContext.navigateToDetail(DatabaseSubScreen.AddPlayResult.route)
            }

            NavEntryNavigateButton(
                titleResId = DatabaseSubScreen.BestList.title,
                icon = Icons.Default.Star,
            ) {
                navContext.navigateToDetail(DatabaseSubScreen.BestList.route)
            }

            // The recent queue only exists under the B30 + R10 rules
            AnimatedVisibility(scoringMode == ArcaeaScoringMode.B30_R10) {
                NavEntryNavigateButton(
                    titleResId = DatabaseSubScreen.R30.title,
                    icon = Icons.Default.History,
                ) {
                    navContext.navigateToDetail(DatabaseSubScreen.R30.route)
                }
            }

            NavEntryNavigateButton(
                titleResId = DatabaseSubScreen.PlayData.title,
                icon = Icons.Default.Build,
            ) {
                navContext.navigateToDetail(DatabaseSubScreen.PlayData.route)
            }
        }
    }
}
