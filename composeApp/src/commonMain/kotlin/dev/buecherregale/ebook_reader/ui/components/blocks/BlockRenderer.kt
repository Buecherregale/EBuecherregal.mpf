@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui.components.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.core.dom.BlockNode
import dev.buecherregale.ebook_reader.core.dom.BlockQuote
import dev.buecherregale.ebook_reader.core.dom.Heading
import dev.buecherregale.ebook_reader.core.dom.ImageBlock
import dev.buecherregale.ebook_reader.core.dom.ListBlock
import dev.buecherregale.ebook_reader.core.dom.ListItem
import dev.buecherregale.ebook_reader.core.dom.Paragraph
import dev.buecherregale.ebook_reader.core.dom.epub.generateNodeId
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.ui.AnnotatedTextBuilder
import dev.buecherregale.ebook_reader.ui.components.rememberImageBitmap
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun BlockRenderer(
    bookId: Uuid,
    block: BlockNode
) {
    when (block) {
        is Paragraph -> ParagraphView(block)
        is Heading -> HeadingView(block)
        is ImageBlock -> ImageBlockView(bookId, block)
        is BlockQuote -> BlockQuoteView(bookId, block)
        is ListBlock -> ListBlockView(bookId, block)
    }
}

@Composable
fun ParagraphView(block: Paragraph) {
    val annotatedString = remember(block) {
        AnnotatedTextBuilder().build(block.inlines, block.id)
    }
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun HeadingView(block: Heading) {
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
    Text(
        text = annotatedString,
        style = style,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun ImageBlockView(
    bookId: Uuid,
    block: ImageBlock,
    bookService: BookService = koinInject()
) {
    val imageBitmap by rememberImageBitmap(block.imageRef) {
        bookService.bookResourceRepository(bookId).load(block.imageRef.resourceFileId)
    }
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
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
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun BlockQuoteView(
    bookId: Uuid,
    block: BlockQuote
) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        block.blocks.forEach { childBlock ->
            BlockRenderer(bookId, childBlock)
        }
    }
}

@Composable
fun ListBlockView(
    bookId: Uuid,
    block: ListBlock
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        block.items.forEachIndexed { index, item ->
            ListItemView(bookId, item, block.ordered, index + 1)
        }
    }
}

@Composable
fun ListItemView(
    bookId: Uuid,
    item: ListItem,
    ordered: Boolean,
    index: Int
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (ordered) "$index." else "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(24.dp)
        )
        Column {
            item.blocks.forEach { childBlock ->
                BlockRenderer(bookId, childBlock)
            }
        }
    }
}
