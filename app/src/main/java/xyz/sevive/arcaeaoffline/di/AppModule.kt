package xyz.sevive.arcaeaoffline.di

import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.plugin.module.dsl.worker
import xyz.sevive.arcaeaoffline.data.BuildFlavor
import xyz.sevive.arcaeaoffline.data.notification.Notifications
import xyz.sevive.arcaeaoffline.database.AppDatabase
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.database.repositories.OcrHistoryRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBufferRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepositoryImpl
import xyz.sevive.arcaeaoffline.datastore.AppDataStoreProvider
import xyz.sevive.arcaeaoffline.datastore.AppPreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.UnstableFlavorPreferencesRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHolder
import xyz.sevive.arcaeaoffline.helpers.DeviceOcrHelper
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.helpers.OcrQueueHelper
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueJob
import xyz.sevive.arcaeaoffline.jobs.R30UpdateJob
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityViewModel
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabaseNavEntryViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult.DatabaseAddPlayResultViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.b30list.DatabaseB30ListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator.DatabaseDeduplicatorViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.manage.DatabaseManageViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.DatabasePlayResultListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.r30list.DatabaseR30ListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreenViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreenViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker.OcrQueueEnqueueCheckerViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.preferences.OcrQueuePreferencesViewModel
import xyz.sevive.arcaeaoffline.ui.screens.overview.OverviewViewModel
import xyz.sevive.arcaeaoffline.ui.screens.settings.SettingsViewModel
import xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert.SettingsUnstableAlertScreenViewModel

val appModule =
    module {
        single { AppDatabase.getDatabase(androidContext()) }
        single { OcrQueueDatabase.getDatabase(androidContext()) }

        single { get<AppDatabase>().ocrHistoryDao() }
        single { get<OcrQueueDatabase>().ocrQueueTaskDao() }
        single { get<OcrQueueDatabase>().ocrQueueEnqueueBufferDao() }

        single { AppDataStoreProvider.appPreferences(androidContext()) }
        single { AppDataStoreProvider.emergencyModePreferences(androidContext()) }
        single { AppDataStoreProvider.ocrQueuePreferences(androidContext()) }
        single { AppDataStoreProvider.unstableFlavorPreferences(androidContext()) }

        single<OcrHistoryRepository>()
        single<OcrQueueTaskRepositoryImpl>().bind(OcrQueueTaskRepository::class)
        single<OcrQueueEnqueueBufferRepository>()

        single<AppPreferencesRepository>()
        single<EmergencyModePreferencesRepository>()
        single<OcrQueuePreferencesRepository>()
        single<UnstableFlavorPreferencesRepository>()

        single { WorkManager.getInstance(androidContext()) }
        single { androidContext().resources }
        single { androidContext().assets }

        single { ArcaeaResourcesStateHolder }
        single { Notifications }
        single { OcrDependencyLoader }
        single { ArcaeaPlayResultValidator }
        single { DeviceOcrHelper }
        single { OcrQueueHelper }
        single { BuildFlavor }
    }

val viewModelModule =
    module {
        viewModel<EmergencyModeActivityViewModel>()
        viewModel<OverviewViewModel>()
        viewModel<DatabaseNavEntryViewModel>()
        viewModel<DatabaseManageViewModel>()
        viewModel<DatabaseAddPlayResultViewModel>()
        viewModel<DatabasePlayResultListViewModel>()
        viewModel<DatabaseB30ListViewModel>()
        viewModel<DatabaseR30ListViewModel>()
        viewModel<DatabaseDeduplicatorViewModel>()
        viewModel<OcrDependenciesScreenViewModel>()
        viewModel<OcrQueueScreenViewModel>()
        viewModel<OcrQueueEnqueueCheckerViewModel>()
        viewModel<OcrQueuePreferencesViewModel>()
        viewModel<OcrFromShareViewModel>()
        viewModel<SettingsViewModel>()
        viewModel<SettingsUnstableAlertScreenViewModel>()
    }

val workerModule =
    module {
        worker<R30UpdateJob>()
        worker<OcrQueueJob>()
        worker<ImageHashesDatabaseBuilderJob>()
    }
