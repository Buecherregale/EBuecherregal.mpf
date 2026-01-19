package dev.buecherregale.ebook_reader.core.dom

import kotlinx.serialization.Serializable

const val DOM_SCHEMA_VERSION = 1

@Serializable
data class DomDocument(
    val schemaVersion: Int,
    val chapter: List<Chapter>
)


@Serializable
data class Chapter(
    val id: String,
    val title: String?,
    val blocks: List<BlockNode>
)

interface DomMigration {
    val fromVersion: Int
    val toVersion: Int
    fun migrate(document: DomDocument): DomDocument
}

@Serializable
sealed interface BlockNode {
    val id: String
}

@Serializable
data class Paragraph(
    override val id: String,
    val inlines: List<InlineNode>
) : BlockNode

@Serializable
data class Heading(
    override val id: String,
    val level: Int,
    val inlines: List<InlineNode>
) : BlockNode

@Serializable
data class ImageBlock(
    override val id: String,
    val imageRef: ImageRef,
    val caption: List<InlineNode>? = null
) : BlockNode

@Serializable
data class ImageRef(
    val id: String,
    val mimeType: String,
    val resourceFileId: String,
)

@Serializable
data class BlockQuote(
    override val id: String,
    val blocks: List<BlockNode>
) : BlockNode

@Serializable
data class ListBlock(
    override val id: String,
    val ordered: Boolean,
    val items: List<ListItem>
) : BlockNode

@Serializable
data class ListItem(
    val id: String,
    val blocks: List<BlockNode>
)

@Serializable
sealed interface InlineNode

@Serializable
data class Text(
    val text: String
) : InlineNode

@Serializable
enum class Emphasis {
    BOLD,
    ITALIC,
}

@Serializable
data class Emphasized(
    val emphasis: Set<Emphasis>,
    val children: List<InlineNode>
) : InlineNode

@Serializable
data class Link(
    val target: LinkTarget,
    val children: List<InlineNode>
) : InlineNode

@Serializable
sealed interface LinkTarget {
    @Serializable
    data class External(val url: String) : LinkTarget
    @Serializable
    data class Internal(val nodeId: String) : LinkTarget
}

@Serializable
data class Ruby(
    val children: List<InlineNode>,
    val ruby: String
) : InlineNode

@Serializable
internal data class RubyAnnotation(
    val text: String
) : InlineNode
