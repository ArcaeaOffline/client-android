package xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import java.util.UUID
import kotlin.time.Duration.Companion.seconds


class DatabaseDeduplicatorViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    //#region Raw grouped play results
    private val _groupByValues = MutableStateFlow(setOf(GroupByValue.SCORE))
    internal val groupByValues = _groupByValues.asStateFlow()

    internal fun setGroupByValues(values: Set<GroupByValue>) {
        _groupByValues.value = values
    }

    private fun PlayResult.groupByKey(groupByValues: Set<GroupByValue>): String {
        val keys = mutableSetOf(songId, ratingClass.toString())

        fun addKey(base: String, desiredKey: String?) {
            keys.add(desiredKey?.let { "${base}${desiredKey}" } ?: "${base}null")
        }

        fun addKey(base: String, value: Int?) = addKey(base, value?.toString())

        if (GroupByValue.SCORE in groupByValues) addKey("s", score)
        if (GroupByValue.PURE in groupByValues) addKey("p", pure)
        if (GroupByValue.FAR in groupByValues) addKey("f", far)
        if (GroupByValue.LOST in groupByValues) addKey("l", lost)
        if (GroupByValue.MAX_RECALL in groupByValues) addKey("mr", maxRecall)
        if (GroupByValue.DATE in groupByValues) addKey("d", date?.toEpochMilli()?.toString())
        if (GroupByValue.CLEAR_TYPE in groupByValues) addKey("ct", clearType?.value)
        if (GroupByValue.MODIFIER in groupByValues) addKey("md", modifier?.value)

        return keys.sorted().joinToString(".")
    }

    private val groupsBuilding = MutableStateFlow(false)
    private val groups = MutableStateFlow(mapOf<String, List<PlayResult>>())

    private suspend fun buildDuplicateGroupsTask(values: Set<GroupByValue>): Map<String, List<PlayResult>> {
        val playResults =
            repositoryContainer.playResultRepo.findAll().firstOrNull() ?: return emptyMap()

        return playResults.sortedBy { it.id }.groupBy { it.groupByKey(values) }
            .filter { it.value.size >= 2 }
    }

    private var buildDuplicateGroupsJob: Job? = null
    internal fun buildDuplicateGroups(values: Set<GroupByValue>) {
        buildDuplicateGroupsJob?.cancel()
        buildDuplicateGroupsJob = viewModelScope.launch(Dispatchers.IO) {
            groupsBuilding.value = true
            groups.value = buildDuplicateGroupsTask(values)
            groupsBuilding.value = false
        }
    }
    //#endregion

    //#region Selections
    private val _selectedUuids = MutableStateFlow(setOf<UUID>())
    val selectedUuids = _selectedUuids.asStateFlow()

    private val autoSelectSemaphore = Semaphore(1)
    private val autoSelectRunning = MutableStateFlow(false)

    fun setPlayResultSelected(uuid: UUID, selected: Boolean) {
        if (selected) _selectedUuids.value += uuid else _selectedUuids.value -= uuid
    }

    fun clearSelectedItems() {
        _selectedUuids.value = emptySet()
    }

    private suspend fun autoSelectTask(mode: AutoSelectMode) {
        autoSelectSemaphore.withPermit {
            autoSelectRunning.value = true

            val playResultUuidsToSelect = when (mode) {
                AutoSelectMode.IDENTICAL -> AutoSelector.identical(groups.value.values)
                AutoSelectMode.PROPERTIES_PRIORITY -> AutoSelector.propertiesPriority(groups.value.values)
                AutoSelectMode.R30_PRIORITY -> AutoSelector.r30Priority(groups.value.values)
            }.map { it.uuid }

            groups.value.values.forEach { playResults ->
                playResults.forEach {
                    setPlayResultSelected(it.uuid, it.uuid in playResultUuidsToSelect)
                }
            }

            autoSelectRunning.value = false
        }
    }

    internal fun autoSelect(mode: AutoSelectMode) {
        viewModelScope.launch(Dispatchers.Default) {
            autoSelectTask(mode)
        }
    }

    fun deleteSelectedItemsInDatabase(uuids: Set<UUID>) {
        viewModelScope.launch(Dispatchers.IO) {
            val playResults =
                repositoryContainer.playResultRepo.findAllByUUID(uuids.toList()).firstOrNull()
                    ?: emptyList()

            repositoryContainer.playResultRepo.deleteBatch(*playResults.toTypedArray())
            clearSelectedItems()
            buildDuplicateGroups(groupByValues.value)
        }
    }
    //#endregion

    //#region Merging
    private suspend fun mergeGroupTask(groupKey: String, newPlayResult: PlayResult) {
        val oldPlayResults = groups.value[groupKey] ?: emptyList()
        repositoryContainer.playResultRepo.upsert(newPlayResult)
        repositoryContainer.playResultRepo.deleteBatch(*oldPlayResults.toTypedArray())
    }

    fun mergeGroup(groupKey: String, newPlayResult: PlayResult) {
        viewModelScope.launch(Dispatchers.IO) {
            mergeGroupTask(groupKey, newPlayResult)
            buildDuplicateGroups(groupByValues.value)
        }
    }

    private val autoMergeSemaphore = Semaphore(1)
    private val autoMergeRunning = MutableStateFlow(false)

    fun autoMerge() {
        viewModelScope.launch(Dispatchers.IO) {
            autoMergeSemaphore.withPermit {
                autoMergeRunning.value = true

                val mergedMap = AutoMerger.merge(groups.value)
                for ((key, playResult) in mergedMap) {
                    if (key !in groups.value) continue
                    mergeGroup(key, playResult)
                }
                buildDuplicateGroups(groupByValues.value)

                autoMergeRunning.value = false
            }
        }
    }
    //#endregion

    //#region UI states
    data class GroupListUiItem(
        val index: Int,
        val key: String,
        val chart: Chart?,
        val playResults: List<PlayResult>,
    )

    data class UiState(
        val isLoading: Boolean = true,
        val listItems: List<GroupListUiItem> = emptyList(),
    )

    private val groupListUiItemLoading = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val groupListUiItems = groups.transformLatest { groups ->
        groupListUiItemLoading.value = true

        val chartCacheMap = mutableMapOf<String, Pair<Song, Difficulty>?>()

        emit(groups.entries.mapIndexed { i, entry ->
            val playResult = entry.value.getOrNull(0)
            val chartKey = playResult?.let { "${it.songId}.${it.ratingClass}" }

            if (chartKey != null && chartCacheMap[chartKey] == null) {
                val song = repositoryContainer.songRepo.find(playResult).firstOrNull()
                val difficulty = repositoryContainer.difficultyRepo.find(playResult).firstOrNull()

                chartCacheMap[chartKey] = if (song != null && difficulty != null) song to difficulty
                else null
            }

            val chart = chartCacheMap[chartKey]?.let {
                ChartFactory.fakeChart(it.first, it.second)
            }

            GroupListUiItem(
                index = i,
                key = entry.key,
                chart = chart,
                playResults = entry.value,
            )
        })

        groupListUiItemLoading.value = false
    }

    private val isLoading = combine(
        groupsBuilding,
        groupListUiItemLoading,
        autoSelectRunning,
        autoMergeRunning,
    ) { b1, b2, b3, b4 -> b1 || b2 || b3 || b4 }

    val uiState = combine(isLoading, groupListUiItems) { isLoading, groupListUiItems ->
        UiState(isLoading = isLoading, listItems = groupListUiItems)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        UiState(),
    )
    //#endregion
}
