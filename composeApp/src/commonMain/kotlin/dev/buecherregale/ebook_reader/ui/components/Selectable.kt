package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.*

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
    onSelected: (SelectedText) -> Unit = {},
    onClick: (Int) -> Boolean = { false }
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentText by rememberUpdatedState(text)
    val currentOnSelected by rememberUpdatedState(onSelected)
    val currentOnClick by rememberUpdatedState(onClick)

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
                detectTapGestures(
                    onTap = { tapOffset ->
                        val result = layoutResult ?: return@detectTapGestures
                        val coords = layoutCoordinates ?: return@detectTapGestures
                        val offset = result.getOffsetForPosition(tapOffset)
                        
                        if (!currentOnClick(offset)) {
                            if (offset in currentText.indices) {
                                val charBounds = result.getBoundingBox(offset)
                                val screenBounds = charBounds.translate(coords.localToWindow(Offset.Zero))

                                currentOnSelected(SelectedText(
                                    offset,
                                    currentText.text,
                                    screenBounds
                                ))
                            }
                        }
                    },
                    onLongPress = { tapOffset ->
                        val result = layoutResult ?: return@detectTapGestures
                        val coords = layoutCoordinates ?: return@detectTapGestures

                        val index = result.getOffsetForPosition(tapOffset)

                        if (index in currentText.indices) {
                            val charBounds = result.getBoundingBox(index)

                            val screenBounds = charBounds.translate(coords.localToWindow(Offset.Zero))

                            currentOnSelected(SelectedText(
                                index,
                                currentText.text,
                                screenBounds
                            ))
                        }
                    }
                )
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
    onSelected: (SelectedText) -> Unit = {},
    onClick: (Int) -> Boolean = { false }
) {
    SelectableText(
        text = AnnotatedString(text),
        style = style,
        modifier = modifier,
        selectedRange = selectedRange,
        onSelected = onSelected,
        onClick = onClick
    )
}
