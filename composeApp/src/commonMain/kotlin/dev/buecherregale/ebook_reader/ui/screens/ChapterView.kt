package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.core.dom.Chapter
import dev.buecherregale.ebook_reader.ui.components.blocks.BlockRenderer

@Composable
fun ChapterView(chapter: Chapter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        chapter.blocks.forEach { block ->
            BlockRenderer(block)
        }
    }
}