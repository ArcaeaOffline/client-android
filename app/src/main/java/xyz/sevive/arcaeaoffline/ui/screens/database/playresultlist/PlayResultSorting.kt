package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

enum class PlayResultSortOrder {
    ASC,
    DESC,
    ;

    fun reverse() = if (this == ASC) DESC else ASC
}

enum class PlayResultSortKey { TITLE, DIFFICULTY, SCORE, PLAY_RATING, DATE }

internal fun sortPlayResultList(
    items: List<PlayResultListUiItem>,
    order: PlayResultSortOrder,
    key: PlayResultSortKey,
): List<PlayResultListUiItem> =
    when (key) {
        PlayResultSortKey.TITLE -> items.sortedWith(titleComparator(order))
        PlayResultSortKey.DIFFICULTY -> items.sortedWith(difficultyComparator(order))
        PlayResultSortKey.SCORE -> items.sortedByValue(order) { it.playResult.score }
        PlayResultSortKey.PLAY_RATING -> items.sortedByValue(order) { it.playRating }
        PlayResultSortKey.DATE -> items.sortedByValue(order) { it.playResult.date?.toEpochMilliseconds() }
    }

private fun titleComparator(order: PlayResultSortOrder): Comparator<PlayResultListUiItem> {
    val byTitle = compareBy<PlayResultListUiItem, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
    return if (order == PlayResultSortOrder.ASC) byTitle else byTitle.reversed()
}

private fun difficultyComparator(order: PlayResultSortOrder): Comparator<PlayResultListUiItem> {
    val byMissing = compareBy<PlayResultListUiItem> { it.difficultySortValue == null }
    val byValue = compareBy<PlayResultListUiItem> { it.difficultySortValue ?: 0 }
    // A fallback value ties with the level's highest known constant; this
    // orders the chart without a constant above that known constant.
    val byKnown = compareBy<PlayResultListUiItem> { !it.hasConstant }
    val byTitle = compareBy<PlayResultListUiItem, String>(String.CASE_INSENSITIVE_ORDER) { it.title }

    return if (order == PlayResultSortOrder.ASC) {
        byMissing.then(byValue).then(byKnown).then(byTitle)
    } else {
        byMissing.then(byValue.reversed()).then(byKnown.reversed()).then(byTitle)
    }
}

private fun <T : Comparable<T>> List<PlayResultListUiItem>.sortedByValue(
    order: PlayResultSortOrder,
    selector: (PlayResultListUiItem) -> T?,
): List<PlayResultListUiItem> {
    val sorted = sortedBy(selector)
    return if (order == PlayResultSortOrder.ASC) sorted else sorted.reversed()
}

private val PlayResultListUiItem.chartConstant: Int?
    get() = display?.chartInfo?.constant?.takeIf { it > 0 }

/**
 * Difficulty key in tenths. Null means the song data is missing, so the chart
 * has no level.
 *
 * A chart without a constant falls back to the top of its displayed level.
 */
private val PlayResultListUiItem.difficultySortValue: Int?
    get() =
        chartConstant
            ?: display?.difficultyWithSong?.let { levelTopConstant(it.rating, it.ratingPlus) }

private val PlayResultListUiItem.hasConstant: Boolean
    get() = chartConstant != null

/**
 * Fallback difficulty key for a chart whose constant is missing, in tenths.
 *
 * Uses the .6/.9 split of the level's tenths. Where the real constants use a
 * coarser grid, the value lands in the gap above the highest real constant.
 */
private fun levelTopConstant(
    rating: Int,
    ratingPlus: Boolean,
): Int = rating * 10 + if (ratingPlus) 9 else 6
