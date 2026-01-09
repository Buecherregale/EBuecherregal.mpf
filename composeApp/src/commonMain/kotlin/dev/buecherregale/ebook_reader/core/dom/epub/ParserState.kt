package dev.buecherregale.ebook_reader.core.dom.epub

import dev.buecherregale.ebook_reader.core.dom.BlockNode
import dev.buecherregale.ebook_reader.core.dom.InlineNode
import dev.buecherregale.ebook_reader.core.dom.Text

internal sealed interface CurrentBlock {
    data class Paragraph(val id: String) : CurrentBlock
    data class Heading(val id: String, val level: Int) : CurrentBlock
}

internal class InlineStack {
    private val stack = ArrayDeque<MutableList<InlineNode>>()

    init {
        stack.addLast(mutableListOf())
    }

    fun push() {
        stack.addLast(mutableListOf())
    }

    fun pop(): List<InlineNode> = stack.removeLast()

    fun add(node: InlineNode) {
        stack.last().add(node)
    }

    fun addText(text: String) {
        if (text.isNotBlank()) {
            add(Text(text))
        }
    }

    fun current(): List<InlineNode> = stack.last()
}

internal class ParseContext(
    val blocks: MutableList<BlockNode>,
    val resources: ResourceResolver
) {
    val inlineStack = InlineStack()
    var currentBlock: CurrentBlock? = null
}