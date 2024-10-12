package xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator

import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult


internal fun List<PlayResult>.mergePlayResults(): PlayResult? {
    if (this.isEmpty()) return null
    if (this.size == 1) return this[0]

    val scoreList = this.map { it.score }
    val pureList = this.mapNotNull { it.pure }
    val farList = this.mapNotNull { it.far }
    val lostList = this.mapNotNull { it.lost }
    val dateList = this.mapNotNull { it.date }
    val maxRecallList = this.mapNotNull { it.maxRecall }
    val clearTypeList = this.mapNotNull { it.clearType }
    val modifierList = this.mapNotNull { it.modifier }
    val commentList =
        listOf("Auto merged from ${this.size} records") + this.mapNotNull { it.comment }

    return PlayResult(
        songId = this[0].songId,
        ratingClass = this[0].ratingClass,
        score = scoreList[0],
        pure = pureList.getOrNull(0),
        far = farList.getOrNull(0),
        lost = lostList.getOrNull(0),
        date = dateList.getOrNull(0),
        maxRecall = maxRecallList.getOrNull(0),
        clearType = clearTypeList.getOrNull(0),
        modifier = modifierList.getOrNull(0),
        comment = commentList.joinToString(" | ")
    )
}

internal object AutoMerger {
    fun merge(groups: Map<String, List<PlayResult>>): Map<String, PlayResult> {
        return groups.mapNotNull { (key, playResults) ->
            val mergedPlayResult = playResults.mergePlayResults()
            mergedPlayResult?.let { key to it }
        }.toMap()
    }
}
