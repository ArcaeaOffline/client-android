package xyz.sevive.arcaeaoffline.ui


import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkManager
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityViewModel
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.components.ChartSelectorViewModel
import xyz.sevive.arcaeaoffline.ui.components.SongIdSelectorViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabaseAddPlayResultViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabaseNavEntryViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabasePlayResultListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabaseR30ListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.b30list.DatabaseB30ListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.manage.DatabaseManageViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreenViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreenViewModel
import xyz.sevive.arcaeaoffline.ui.screens.overview.OverviewViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ViewModels
        initializer {
            EmergencyModeActivityViewModel(application().dataStoreRepositoryContainer.emergencyModePreferences)
        }

        initializer {
            OverviewViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer.potentialRepo
            )
        }

        initializer {
            DatabaseNavEntryViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseManageViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseAddPlayResultViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabasePlayResultListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseB30ListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseR30ListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            SongIdSelectorViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            ChartSelectorViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            OcrDependenciesScreenViewModel(application())
        }

        initializer {
            OcrQueueScreenViewModel(
                WorkManager.getInstance(application()),
                application().arcaeaOfflineDatabaseRepositoryContainer,
                application().ocrQueueDatabaseRepositoryContainer,
                application().dataStoreRepositoryContainer.ocrQueuePreferences,
            )
        }

        initializer {
            OcrFromShareViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer,
                application().appDatabaseRepositoryContainer
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [ArcaeaOfflineApplication].
 */
fun CreationExtras.application(): ArcaeaOfflineApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ArcaeaOfflineApplication)
