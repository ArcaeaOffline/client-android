package xyz.sevive.arcaeaoffline.di

import android.content.Context
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.plugin.module.dsl.worker
import xyz.sevive.arcaeaoffline.core.di.coreModule
import xyz.sevive.arcaeaoffline.database.AppDatabase
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.database.daos.OcrHistoryDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueEnqueueBufferDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueTaskDao
import xyz.sevive.arcaeaoffline.database.repositories.OcrHistoryRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBufferRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepositoryImpl
import xyz.sevive.arcaeaoffline.datastore.AppPreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.UnstableFlavorPreferencesRepository
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueEnqueueCheckerJob
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

internal fun createAppDatabase(context: Context): AppDatabase = AppDatabase.getDatabase(context)

internal fun ocrHistoryDao(db: AppDatabase) = db.ocrHistoryDao()

internal fun createOcrQueueDatabase(context: Context): OcrQueueDatabase = OcrQueueDatabase.getDatabase(context)

internal fun ocrQueueTaskDao(db: OcrQueueDatabase) = db.ocrQueueTaskDao()

internal fun ocrQueueEnqueueBufferDao(db: OcrQueueDatabase) = db.ocrQueueEnqueueBufferDao()

val appModule =
    module {
        includes(coreModule)

        single<AppDatabase> { create(::createAppDatabase) }
        single<OcrHistoryDao> { create(::ocrHistoryDao) }
        single<OcrHistoryRepository>()

        single<OcrQueueDatabase> { create(::createOcrQueueDatabase) }
        single<OcrQueueTaskDao> { create(::ocrQueueTaskDao) }
        single<OcrQueueEnqueueBufferDao> { create(::ocrQueueEnqueueBufferDao) }
        single<OcrQueueTaskRepositoryImpl>().bind(OcrQueueTaskRepository::class)
        single<OcrQueueEnqueueBufferRepository>()

        single<AppPreferencesRepository>()
        single<EmergencyModePreferencesRepository>()
        single<OcrQueuePreferencesRepository>()
        single<UnstableFlavorPreferencesRepository>()

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

        worker<R30UpdateJob>()
        worker<OcrQueueJob>()
        worker<ImageHashesDatabaseBuilderJob>()
        worker<OcrQueueEnqueueCheckerJob>()
    }
