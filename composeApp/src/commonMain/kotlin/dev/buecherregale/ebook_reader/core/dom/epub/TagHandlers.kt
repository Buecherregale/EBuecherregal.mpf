@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.core.dom.epub

import dev.buecherregale.ebook_reader.core.dom.Emphasis
import dev.buecherregale.ebook_reader.core.dom.Emphasized
import dev.buecherregale.ebook_reader.core.dom.Heading
import dev.buecherregale.ebook_reader.core.dom.ImageBlock
import dev.buecherregale.ebook_reader.core.dom.Paragraph
import kotlin.uuid.ExperimentalUuidApi

internal interface TagHandler {
    suspend fun onStart(ctx: ParseContext, attrs: Map<String, String>)
    fun onEnd(ctx: ParseContext)
}

internal class TagHandlerRegistry {

    private val handlers = mutableMapOf<String, TagHandler>()

    fun register(tag: String, handler: TagHandler) {
        handlers[tag.lowercase()] = handler
    }

    fun get(tag: String): TagHandler? =
        handlers[tag.lowercase()]
}

internal class ParagraphHandler : TagHandler {

    override suspend fun onStart(ctx: ParseContext, attrs: Map<String, String>) {
        ctx.currentBlock = CurrentBlock.Paragraph(generateNodeId())
        ctx.inlineStack.push()
    }

    override fun onEnd(ctx: ParseContext) {
        val block = ctx.currentBlock as? CurrentBlock.Paragraph ?: return
        val inlines = ctx.inlineStack.pop()

        ctx.blocks.add(
            Paragraph(
                id = block.id,
                inlines = inlines
            )
        )
        ctx.currentBlock = null
    }
}

internal class HeadingHandler(private val level: Int) : TagHandler {

    override suspend fun onStart(ctx: ParseContext, attrs: Map<String, String>) {
        ctx.currentBlock = CurrentBlock.Heading(generateNodeId(), level)
        ctx.inlineStack.push()
    }

    override fun onEnd(ctx: ParseContext) {
        val block = ctx.currentBlock as? CurrentBlock.Heading ?: return
        val inlines = ctx.inlineStack.pop()

        ctx.blocks.add(
            Heading(
                id = block.id,
                level = block.level,
                inlines = inlines
            )
        )
        ctx.currentBlock = null
    }
}

internal class EmphasisHandler(
    private val emphasis: Emphasis
) : TagHandler {

    override suspend fun onStart(ctx: ParseContext, attrs: Map<String, String>) {
        ctx.inlineStack.push()
    }

    override fun onEnd(ctx: ParseContext) {
        val children = ctx.inlineStack.pop()
        ctx.inlineStack.add(
            Emphasized(
                emphasis = setOf(emphasis),
                children = children
            )
        )
    }
}

internal class ImageHandler : TagHandler {

    override suspend fun onStart(ctx: ParseContext, attrs: Map<String, String>) {
        val src = attrs["src"]
            ?: attrs["href"]
            ?: return
        val ref = ctx.resources.resolvePath(ctx.parsingItem.href, src)
        val image = ctx.resources.extractImage(ref)

        ctx.blocks.add(
            ImageBlock(
                id = generateNodeId(),
                imageRef = image
            )
        )
    }

    override fun onEnd(ctx: ParseContext) {
        // nothing
    }
}