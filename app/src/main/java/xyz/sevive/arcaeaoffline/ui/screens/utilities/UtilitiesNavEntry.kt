package xyz.sevive.arcaeaoffline.ui.screens.utilities

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreen
import xyz.sevive.arcaeaoffline.ui.navigation.UtilitiesSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UtilitiesNavEntry(modifier: Modifier = Modifier) {
    val navContext = LocalListDetailNavigationContext.current

    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreen.Utilities.title)) })
        },
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(it),
            contentPadding = it,
        ) {
            item {
                NavEntryNavigateButton(
                    titleResId = UtilitiesSubScreen.Calculator.title,
                    icon = Icons.Default.Calculate,
                ) {
                    navContext.navigateToDetail(UtilitiesSubScreen.Calculator.route)
                }
            }
        }
    }
}
