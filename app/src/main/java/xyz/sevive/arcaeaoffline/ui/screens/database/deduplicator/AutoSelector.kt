package xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator

import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult


internal object AutoSelector {
    private fun PlayResult.identicalKey(): String {
        return buildString {
            append(songId)
            append(ratingClass.value)
            append(score)
            append(pure)
            append(far)
            append(lost)
            append(date?.toEpochMilli())
            append(maxRecall)
            append(clearType?.value)
            append(modifier?.value)
        }
    }

    private fun PlayResult.countNotNullProperties(): Int {
        return listOf(pure, far, lost, date, maxRecall, clearType, modifier).count { it != null }
    }

    fun identical(groupValues: Collection<List<PlayResult>>): List<PlayResult> {
        return groupValues.map { playResults ->
            playResults  // for each group of the duplicate groups...
                .groupBy { it.identicalKey() }  // group by custom identical keys
                .asSequence()  // IDE says this could improve performance
                .map { it.value }  // we only care about the play results
                .filter { it.size > 1 }  // choose those lists that have more than 2 identical items
                .map { it.sortedBy { playResult -> playResult.id } }
                .map { it.drop(1) }  // keep the first item not selected
                .flatten()  // pls ignore me im just preventing the formatter joining this beautiful call chain to an awful single line
                .toList()
        }.flatten()
    }

    fun propertiesPriority(groupValues: Collection<List<PlayResult>>): List<PlayResult> {
        return groupValues.flatMap { playResults ->
            val propertiesCount = playResults.map { it.countNotNullProperties() }
            val mostPropertyItemIndex = propertiesCount.indexOf(propertiesCount.max())
            val itemToKeep = playResults.getOrNull(mostPropertyItemIndex)

            if (itemToKeep != null) playResults - itemToKeep
            else emptyList()
        }
    }

    fun r30Priority(groupValues: Collection<List<PlayResult>>): List<PlayResult> {
        return groupValues.flatMap { playResults ->
            val itemToKeep = playResults.find { it.date != null }

            if (itemToKeep != null) playResults - itemToKeep
            else emptyList()
        }
    }
}
