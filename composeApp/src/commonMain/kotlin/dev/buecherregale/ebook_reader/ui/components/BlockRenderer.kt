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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.core.dom.*
import dev.buecherregale.ebook_reader.core.dom.epub.generateNodeId
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.ui.AnnotatedTextBuilder
import dev.buecherregale.ebook_reader.ui.TAG_LINK
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun BlockRenderer(
    bookId: Uuid,
    block: BlockNode,
    selectedRange: TextRange? = null,
    selectedBlockId: String? = null,
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {}
) {
    val range = if (block.id == selectedBlockId) selectedRange else null
    
    when (block) {
        is Paragraph -> ParagraphView(block, range, onSelected, onLinkClick)
        is Heading -> HeadingView(block, range, onSelected, onLinkClick)
        is ImageBlock -> ImageBlockView(bookId, block, range, onSelected = onSelected, onLinkClick = onLinkClick)
        is BlockQuote -> BlockQuoteView(bookId, block, selectedRange, selectedBlockId, onSelected, onLinkClick)
        is ListBlock -> ListBlockView(bookId, block, selectedRange, selectedBlockId, onSelected, onLinkClick)
    }
}

private fun handleLinkClick(
    annotatedString: AnnotatedString,
    offset: Int,
    uriHandler: UriHandler,
    onLinkClick: (LinkTarget) -> Unit
): Boolean {
    val annotation = annotatedString.getStringAnnotations(TAG_LINK, offset, offset)
        .firstOrNull()
    
    return if (annotation != null) {
        val target = Json.decodeFromString<LinkTarget>(annotation.item)
        if (target is LinkTarget.External) {
            uriHandler.openUri(target.url)
        } else {
            onLinkClick(target)
        }
        true
    } else {
        false
    }
}

@Composable
fun ParagraphView(
    block: Paragraph,
    selectedRange: TextRange? = null,
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {}
) {
    val annotatedString = remember(block) {
        AnnotatedTextBuilder().build(block.inlines, block.id)
    }
    val uriHandler = LocalUriHandler.current

    SelectableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        selectedRange = selectedRange,
        onSelected = { onSelected(it, block.id) },
        onClick = { offset ->
            handleLinkClick(annotatedString, offset, uriHandler, onLinkClick)
        }
    )
}

@Composable
fun HeadingView(
    block: Heading,
    selectedRange: TextRange? = null,
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {}
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
    val uriHandler = LocalUriHandler.current

    SelectableText(
        text = annotatedString,
        style = style,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        selectedRange = selectedRange,
        onSelected = { onSelected(it, block.id) },
        onClick = { offset ->
            handleLinkClick(annotatedString, offset, uriHandler, onLinkClick)
        }
    )
}

@Composable
fun ImageBlockView(
    bookId: Uuid,
    block: ImageBlock,
    selectedRange: TextRange? = null,
    bookService: BookService = koinInject(),
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {}
) {
    val imageBitmap by rememberImageBitmap(block.imageRef) {
        bookService.bookResourceRepository(bookId).load(block.imageRef.resourceFileId)
    }
    val uriHandler = LocalUriHandler.current
    
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
                selectedRange = selectedRange,
                onSelected = { onSelected(it, block.id) },
                onClick = { offset ->
                    handleLinkClick(annotatedString, offset, uriHandler, onLinkClick)
                }
            )
        }
    }
}

@Composable
fun BlockQuoteView(
    bookId: Uuid,
    block: BlockQuote,
    selectedRange: TextRange? = null,
    selectedBlockId: String? = null,
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        block.blocks.forEach { childBlock ->
            BlockRenderer(bookId, childBlock, selectedRange, selectedBlockId, onSelected, onLinkClick)
        }
    }
}

@Composable
fun ListBlockView(
    bookId: Uuid,
    block: ListBlock,
    selectedRange: TextRange? = null,
    selectedBlockId: String? = null,
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        block.items.forEachIndexed { index, item ->
            ListItemView(bookId, item, block.ordered, index + 1, selectedRange, selectedBlockId, onSelected, onLinkClick)
        }
    }
}

@Composable
fun ListItemView(
    bookId: Uuid,
    item: ListItem,
    ordered: Boolean,
    index: Int,
    selectedRange: TextRange? = null,
    selectedBlockId: String? = null,
    onSelected: (SelectedText, String) -> Unit = { _, _ -> },
    onLinkClick: (LinkTarget) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectableText(
            text = if (ordered) "$index." else "•",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(24.dp),
            onSelected = { /* Bullet/number selection not supported yet */ }
        )
        Column {
            item.blocks.forEach { childBlock ->
                BlockRenderer(bookId, childBlock, selectedRange, selectedBlockId, onSelected, onLinkClick)
            }
        }
    }
}
