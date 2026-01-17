package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import co.touchlab.kermit.Logger

data class SelectedText(
    val index: Int,
    val text: String,
    val bounds: Rect
)

@Composable
fun SelectableText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    selectedRange: TextRange? = null,
    onSelected: (SelectedText) -> Unit = { Logger.d { "selected: $it" } }
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    val displayText = remember(text, selectedRange) {
        if (selectedRange != null && selectedRange.start >= 0 && selectedRange.end <= text.length) {
            buildAnnotatedString {
                append(text)
                addStyle(
                    SpanStyle(background = color),
                    selectedRange.start,
                    selectedRange.end
                )
            }
        } else {
            text
        }
    }

    Text(
        text = displayText,
        style = style,
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                layoutCoordinates = coordinates
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val result = layoutResult ?: return@detectTapGestures
                    val coords = layoutCoordinates ?: return@detectTapGestures

                    val index = result.getOffsetForPosition(tapOffset)

                    if (index in text.indices) {
                        val charBounds = result.getBoundingBox(index)

                        val screenBounds = charBounds.translate(coords.localToWindow(Offset.Zero))

                        onSelected(SelectedText(
                            index,
                            text.text,
                            screenBounds
                        ))
                    }
                }
            },
        onTextLayout = { layoutResult = it }
    )
}

@Composable
fun SelectableText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    selectedRange: TextRange? = null,
    onSelected: (SelectedText) -> Unit = {}
) {
    SelectableText(
        text = AnnotatedString(text),
        style = style,
        modifier = modifier,
        selectedRange = selectedRange,
        onSelected = onSelected
    )
}
