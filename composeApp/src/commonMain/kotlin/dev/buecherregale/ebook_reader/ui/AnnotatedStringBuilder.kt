@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import dev.buecherregale.ebook_reader.core.dom.Emphasis
import dev.buecherregale.ebook_reader.core.dom.Emphasized
import dev.buecherregale.ebook_reader.core.dom.InlineNode
import dev.buecherregale.ebook_reader.core.dom.Link
import dev.buecherregale.ebook_reader.core.dom.LinkTarget
import dev.buecherregale.ebook_reader.core.dom.Text
import kotlinx.serialization.serializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface TextAnnotation {

    data class Link(
        val target: LinkTarget
    ) : TextAnnotation

    data class DomPosition(
        val nodeId: Uuid,
        val offsetInNode: Int
    ) : TextAnnotation
}

const val TAG_LINK = "LINK"
const val TAG_POS = "POS"

private fun TextAnnotation.toTag(): Pair<String, String> =
    when (this) {
        is TextAnnotation.Link ->
            TAG_LINK to serializer<LinkTarget>().toString()

        is TextAnnotation.DomPosition ->
            TAG_POS to "$nodeId:$offsetInNode"
    }

class AnnotatedTextBuilder {

    private val builder = AnnotatedString.Builder()
    private var globalOffset = 0

    fun build(
        inlines: List<InlineNode>,
        baseNodeId: Uuid
    ): AnnotatedString {
        appendInlineNodes(inlines, baseNodeId)
        return builder.toAnnotatedString()
    }

    private fun appendInlineNodes(
        nodes: List<InlineNode>,
        baseNodeId: Uuid
    ) {
        for (node in nodes) {
            when (node) {

                is Text -> {
                    val start = globalOffset
                    builder.append(node.text)
                    val end = builder.length

                    builder.addStringAnnotation(
                        tag = TAG_POS,
                        annotation = "$baseNodeId:${node.text.length}",
                        start = start,
                        end = end
                    )

                    globalOffset = end
                }

                is Emphasized -> {
                    val style = when {
                        Emphasis.BOLD in node.emphasis ->
                            SpanStyle(fontWeight = FontWeight.Bold)
                        Emphasis.ITALIC in node.emphasis ->
                            SpanStyle(fontStyle = FontStyle.Italic)
                        else -> SpanStyle()
                    }

                    builder.withStyle(style) {
                        appendInlineNodes(node.children, baseNodeId)
                    }
                }

                is Link -> {
                    val start = globalOffset
                    appendInlineNodes(node.children, baseNodeId)
                    val end = builder.length

                    val (tag, value) = TextAnnotation.Link(node.target).toTag()
                    builder.addStringAnnotation(tag, value, start, end)

                    builder.addStyle(
                        SpanStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline
                        ),
                        start,
                        end
                    )
                }
            }
        }
    }
}
