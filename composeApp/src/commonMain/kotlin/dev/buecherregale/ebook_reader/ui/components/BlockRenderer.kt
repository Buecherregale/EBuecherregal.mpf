@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.core.dom.*
import dev.buecherregale.ebook_reader.core.dom.epub.generateNodeId
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.ui.AnnotatedTextBuilder
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun BlockRenderer(
    bookId: Uuid,
    block: BlockNode,
    onSelected: (SelectedText) -> Unit = {},
) {
    when (block) {
        is Paragraph -> ParagraphView(block, onSelected)
        is Heading -> HeadingView(block, onSelected)
        is ImageBlock -> ImageBlockView(bookId, block, onSelected = onSelected)
        is BlockQuote -> BlockQuoteView(bookId, block, onSelected)
        is ListBlock -> ListBlockView(bookId, block, onSelected)
    }
}

@Composable
fun ParagraphView(
    block: Paragraph,
    onSelected: (SelectedText) -> Unit = {},
) {
    val annotatedString = remember(block) {
        AnnotatedTextBuilder().build(block.inlines, block.id)
    }
    SelectableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        onSelected = onSelected
    )
}

@Composable
fun HeadingView(block: Heading,
                onSelected: (SelectedText) -> Unit = {}
) {
    val annotatedString = remember(block) {
        AnnotatedTextBuilder().build(block.inlines, block.id)
    }
    val style = when (block.level) {
        1 -> MaterialTheme.typography.headlineLarge
        2 -> MaterialTheme.typography.headlineMedium
        3 -> MaterialTheme.typography.headlineSmall
        4 -> MaterialTheme.typography.titleLarge
        5 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }
    SelectableText(
        text = annotatedString,
        style = style,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        onSelected = onSelected
    )
}

@Composable
fun ImageBlockView(
    bookId: Uuid,
    block: ImageBlock,
    bookService: BookService = koinInject(),
    onSelected: (SelectedText) -> Unit = {}
) {
    val imageBitmap by rememberImageBitmap(block.imageRef) {
        bookService.bookResourceRepository(bookId).load(block.imageRef.resourceFileId)
    }
    
    Column(
        modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
        
        block.caption?.let { caption ->
            val annotatedString = remember(caption) {
                AnnotatedTextBuilder().build(caption, generateNodeId())
            }
            SelectableText(
                text = annotatedString,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp),
                onSelected = onSelected
            )
        }
    }
}

@Composable
fun BlockQuoteView(
    bookId: Uuid,
    block: BlockQuote,
    onSelected: (SelectedText) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        block.blocks.forEach { childBlock ->
            BlockRenderer(bookId, childBlock, onSelected)
        }
    }
}

@Composable
fun ListBlockView(
    bookId: Uuid,
    block: ListBlock,
    onSelected: (SelectedText) -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        block.items.forEachIndexed { index, item ->
            ListItemView(bookId, item, block.ordered, index + 1, onSelected)
        }
    }
}

@Composable
fun ListItemView(
    bookId: Uuid,
    item: ListItem,
    ordered: Boolean,
    index: Int,
    onSelected: (SelectedText) -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectableText(
            text = if (ordered) "$index." else "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(24.dp),
            onSelected = onSelected
        )
        Column {
            item.blocks.forEach { childBlock ->
                BlockRenderer(bookId, childBlock)
            }
        }
    }
}
