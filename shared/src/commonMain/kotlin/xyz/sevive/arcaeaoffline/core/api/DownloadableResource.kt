package xyz.sevive.arcaeaoffline.core.api

/** A file published under the versioned publish directory; [fileName] is the path segment below "{version}/". */
enum class DownloadableResource(
    val fileName: String,
) {
    PACKLIST("packlist"),
    SONGLIST("songlist"),
    CHART_INFO_DATABASE("ci.db"),
    IMAGE_HASHES_DATABASE("ih.db"),
}
