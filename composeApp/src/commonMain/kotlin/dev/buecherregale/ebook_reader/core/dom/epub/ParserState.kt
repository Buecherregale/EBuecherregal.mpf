package dev.buecherregale.ebook_reader.core.dom.epub

import dev.buecherregale.ebook_reader.core.dom.BlockNode
import dev.buecherregale.ebook_reader.core.dom.InlineNode
import dev.buecherregale.ebook_reader.core.dom.Text
import dev.buecherregale.ebook_reader.core.dom.epub.xml.Item

internal sealed interface CurrentBlock {
    data class Paragraph(val id: String) : CurrentBlock
    data class Heading(val id: String, val level: Int) : CurrentBlock
}

internal class StackFrame(
    val nodes: MutableList<InlineNode>,
    val attributes: MutableMap<String, String> = mutableMapOf()
)

internal class InlineStack {
    private val stack = ArrayDeque<StackFrame>()

    init {
        stack.addLast(StackFrame(mutableListOf()))
    }

    fun push() {
        stack.addLast(StackFrame(mutableListOf()))
    }

    fun pop(): StackFrame = stack.removeLast()

    fun add(node: InlineNode) {
        stack.last().nodes.add(node)
    }

    fun addText(text: String) {
        if (text.isNotBlank()) {
            add(Text(text))
        }
    }

    fun currentAttributes(): MutableMap<String, String> = stack.last().attributes
}

internal class ParseContext(
    val parsingItem: Item,
    val blocks: MutableList<BlockNode>,
    val resources: ResourceResolver
) {
    val inlineStack = InlineStack()
    var currentBlock: CurrentBlock? = null
}
