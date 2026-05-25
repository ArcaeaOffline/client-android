package xyz.sevive.arcaeaoffline.core.database.extensions

import androidx.annotation.IntRange
import androidx.sqlite.SQLiteStatement

internal fun SQLiteStatement.getIntOrNull(
    @IntRange(from = 0) index: Int,
): Int? = if (this.isNull(index)) null else this.getInt(index)

internal fun SQLiteStatement.getLongOrNull(
    @IntRange(from = 0) index: Int,
): Long? = if (this.isNull(index)) null else this.getLong(index)

internal fun SQLiteStatement.getTextOrNull(
    @IntRange(from = 0) index: Int,
): String? = if (this.isNull(index)) null else this.getText(index)

internal fun SQLiteStatement.bindIntOrNull(
    @IntRange(from = 1) index: Int,
    value: Int?,
) = if (value == null) this.bindNull(index) else this.bindInt(index, value)

internal fun SQLiteStatement.bindLongOrNull(
    @IntRange(from = 1) index: Int,
    value: Long?,
) = if (value == null) this.bindNull(index) else this.bindLong(index, value)

internal fun SQLiteStatement.bindTextOrNull(
    @IntRange(from = 1) index: Int,
    value: String?,
) = if (value == null) this.bindNull(index) else this.bindText(index, value)
