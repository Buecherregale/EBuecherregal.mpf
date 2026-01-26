@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.core.dom.Chapter
import dev.buecherregale.ebook_reader.core.dom.LinkTarget
import dev.buecherregale.ebook_reader.ui.components.BlockRenderer
import dev.buecherregale.ebook_reader.ui.components.SelectedText
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ChapterView(
    bookId: Uuid,
    chapter: Chapter,
    scrollState: ScrollState,
    selectedRange: TextRange? = null,
    selectedBlockId: String? = null,
    onToggleMenu: () -> Unit = {},
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {},
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onShowMenu: () -> Unit,
    onHideMenu: () -> Unit,
) {
    var swipeDistance by remember { mutableStateOf(0f) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .scan(Pair(0, 0)) { acc, value -> Pair(acc.second, value) }
            .drop(1)
            .map { (old, new) -> if (new > old) 1 else if (new < old) -1 else 0 }
            .filter { it != 0 }
            .distinctUntilChanged()
            .collect {
                if (it > 0) onHideMenu() else onShowMenu()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onToggleMenu() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        swipeDistance += dragAmount.x
                    },
                    onDragEnd = {
                        when {
                            swipeDistance > 200 -> onSwipeRight()
                            swipeDistance < -200 -> onSwipeLeft()
                        }
                        swipeDistance = 0f
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        chapter.blocks.forEach { block ->
            BlockRenderer(bookId, block, selectedRange, selectedBlockId, onSelected, onLinkClick)
        }
    }
}
