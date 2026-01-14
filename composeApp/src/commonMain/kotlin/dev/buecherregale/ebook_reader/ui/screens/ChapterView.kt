@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.core.dom.Chapter
import dev.buecherregale.ebook_reader.ui.components.BlockRenderer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ChapterView(
    bookId: Uuid,
    chapter: Chapter,
    onToggleMenu: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onToggleMenu() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        chapter.blocks.forEach { block ->
            BlockRenderer(bookId, block)
        }
    }
}