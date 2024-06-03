package xyz.sevive.arcaeaoffline.core.database.migrations

import android.database.Cursor


internal fun Cursor.getIntOrNull(columnName: String): Int? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) null else getInt(columnIndex)
}

internal fun Cursor.getLongOrNull(columnName: String): Long? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) null else getLong(columnIndex)
}

internal fun Cursor.getStringOrNull(columnName: String): String? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) null else getString(columnIndex)
}
