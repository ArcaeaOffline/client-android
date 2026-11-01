package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

/**
 * Draws [text] with the half-open [ranges] emphasized in the primary color.
 *
 * Ranges are clamped to the text and sorted. A range that overlaps the previous
 * one is skipped rather than merged.
 */
@Composable
fun HighlightedText(
    text: String,
    ranges: List<IntRange>,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val highlightColor = MaterialTheme.colorScheme.primary
    val annotated =
        remember(text, ranges, highlightColor) {
            if (ranges.isEmpty()) {
                AnnotatedString(text)
            } else {
                buildAnnotatedString {
                    var cursor = 0
                    ranges.sortedBy { it.first }.forEach { range ->
                        val start = range.first.coerceIn(0, text.length)
                        val end = (range.last + 1).coerceIn(start, text.length)
                        if (start < cursor || start == end) return@forEach

                        if (start > cursor) append(text.substring(cursor, start))
                        withStyle(
                            SpanStyle(
                                color = highlightColor,
                                background = highlightColor.copy(alpha = 0.15f),
                                fontWeight = FontWeight.Bold,
                            ),
                        ) {
                            append(text.substring(start, end))
                        }
                        cursor = end
                    }
                    if (cursor < text.length) append(text.substring(cursor))
                }
            }
        }

    Text(annotated, modifier, color = color, style = style, maxLines = maxLines, overflow = overflow)
}
