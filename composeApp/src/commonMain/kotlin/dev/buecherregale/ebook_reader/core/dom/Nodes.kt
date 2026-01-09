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

sealed interface BlockNode {
    val id: String
}

data class Paragraph(
    override val id: String,
    val inlines: List<InlineNode>
) : BlockNode

data class Heading(
    override val id: String,
    val level: Int,
    val inlines: List<InlineNode>
) : BlockNode

data class ImageBlock(
    override val id: String,
    val imageRef: ImageRef,
    val caption: List<InlineNode>? = null
) : BlockNode

data class ImageRef(
    val id: String,
    val mimeType: String,
    val bytes: ByteArray // or lazy loader
)

data class BlockQuote(
    override val id: String,
    val blocks: List<BlockNode>
) : BlockNode

data class ListBlock(
    override val id: String,
    val ordered: Boolean,
    val items: List<ListItem>
) : BlockNode

data class ListItem(
    val id: String,
    val blocks: List<BlockNode>
)

sealed interface InlineNode

data class Text(
    val text: String
) : InlineNode

enum class Emphasis {
    BOLD,
    ITALIC,
    UNDERLINE
}

data class Emphasized(
    val emphasis: Set<Emphasis>,
    val children: List<InlineNode>
) : InlineNode

data class Link(
    val target: LinkTarget,
    val children: List<InlineNode>
) : InlineNode

sealed interface LinkTarget {
    data class External(val url: String) : LinkTarget
    data class Internal(val nodeId: String) : LinkTarget
}

// inline image
