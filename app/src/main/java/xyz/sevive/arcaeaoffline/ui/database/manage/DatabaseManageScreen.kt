package xyz.sevive.arcaeaoffline.ui.database.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer


@Composable
fun DatabaseManageScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatabaseManageViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    SubScreenContainer(onNavigateUp = onNavigateUp,
        title = { Text(stringResource(R.string.database_manage_title)) }) {
        LazyColumn(
            modifier.padding(dimensionResource(R.dimen.general_page_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))
        ) {
            item {
                DatabaseManageImport(viewModel)
            }

            item {
                DatabaseManageExport(viewModel, Modifier.fillMaxWidth())
            }
        }
    }
}
