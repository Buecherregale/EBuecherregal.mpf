package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import co.touchlab.kermit.Logger

data class SelectedText(
    val index: Int,
    val text: String,
    val position: Offset
)

@Composable
fun SelectableText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onSelected: (SelectedText) -> Unit = { Logger.d { "selected: $it" } }
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Text(
        text = text,
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

                        val screenPosition = coords.localToWindow(
                            Offset(
                                charBounds.left,
                                charBounds.top
                            )
                        )

                        onSelected(SelectedText(
                            index,
                            text.text,
                            screenPosition
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
    onSelected: (SelectedText) -> Unit = {}
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Text(
        text = text,
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

                        val screenPosition = coords.localToWindow(
                            Offset(
                                charBounds.left,
                                charBounds.top
                            )
                        )

                        onSelected(SelectedText(
                            index,
                            text,
                            screenPosition
                        ))
                    }
                }
            },
        onTextLayout = { layoutResult = it }
    )
}