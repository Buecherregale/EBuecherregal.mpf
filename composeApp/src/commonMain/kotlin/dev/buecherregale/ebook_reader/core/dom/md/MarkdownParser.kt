package dev.buecherregale.ebook_reader.core.dom.md

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.dom.*
import dev.buecherregale.ebook_reader.core.dom.epub.generateNodeId
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.BookMetadata
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class MarkdownFormatDetector: FormatDetector {

    /**
     * Detects only Markdown documents by checking if the given file has the `.md` extension or the directory contains
     * one such file at the *top level*.
     *
     * @param file a single md file or a folder containing one
     * @param fileService the platform file service to read [dev.buecherregale.ebook_reader.core.service.filesystem.FileMetadata]
     *
     * @return [DocumentFormat.MARKDOWN] or `null` if invalid
     */
    override suspend fun detect(
        file: FileRef,
        fileService: FileService
    ): DocumentFormat? {
        val meta = fileService.getMetadata(file)
        return if (meta.isDirectory) {
            if(fileService.listChildren(file)
                    .any { detect(it, fileService) == DocumentFormat.MARKDOWN }) DocumentFormat.MARKDOWN else null
        } else {
            if (meta.extension == ".md") DocumentFormat.MARKDOWN else null
        }
    }

}

/**
 * In this application, a valid `Markdown` document is either a single file with the `.md` extension
 * *or* a directory containing one or more valid Markdown documents.
 *
 * This is validated via [MarkdownFormatDetector.detect].
 */
@OptIn(ExperimentalUuidApi::class)
class MarkdownParser : DocumentParser {
    override val format: DocumentFormat = DocumentFormat.MARKDOWN

    override suspend fun parse(
        targetId: Uuid,
        file: FileRef,
        fileService: FileService,
        resourceRepository: ResourceRepository
    ): Pair<Book, DomDocument> {
        val meta = fileService.getMetadata(file)
        val book = Book(
            id = targetId,
            progress = 0.0,
            metadata = BookMetadata(
                title = meta.name,
                author = "",
                isbn = "",
                language = Locale.current
            ))
        if (!meta.isDirectory)
            return book to DomDocument(
                schemaVersion = DOM_SCHEMA_VERSION,
                chapter = listOf(parseMarkdownFile(file, file, fileService, resourceRepository))
            )
        val chapters = fileService.listChildren(file)
            .map { parseMarkdownFile(it, file, fileService, resourceRepository) }
        return book to DomDocument(
            schemaVersion = DOM_SCHEMA_VERSION,
            chapter = chapters,
        )
    }

    override suspend fun getCoverBytes(
        file: FileRef,
        fileService: FileService
    ): ByteArray? {
        // no cover for Markdown
        return null
    }

    private suspend fun parseMarkdownFile(toParse: FileRef,
                                  baseFile: FileRef,
                                  fileService: FileService,
                                  resourceRepository: ResourceRepository): Chapter {
        val md = fileService.read(toParse)
        val blocks = parseBlocks(md, baseFile, fileService, resourceRepository)

        return Chapter(
            id = generateNodeId(),
            title = extractTitle(blocks),
            blocks = blocks
        )
    }

    private suspend fun parseBlocks(markdown: String,
                                    baseFile: FileRef,
                                    fileService: FileService,
                                    resourceRepository: ResourceRepository): List<BlockNode> {
        val lines = markdown.lines()
        val blocks = mutableListOf<BlockNode>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            when {
                line.isBlank() -> {
                    i++
                }

                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length
                    val content = line.drop(level).trim()
                    blocks += Heading(
                        id = generateNodeId(),
                        level = level,
                        inlines = parseInlines(content)
                    )
                    i++
                }

                line.startsWith(">") -> {
                    val quotedLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].startsWith(">")) {
                        quotedLines += lines[i].removePrefix(">").trim()
                        i++
                    }
                    blocks += BlockQuote(
                        id = generateNodeId(),
                        blocks = parseBlocks(quotedLines.joinToString("\n"), baseFile, fileService, resourceRepository)
                    )
                }

                line.startsWith("- ") || line.startsWith("* ") || line.matches(Regex("""\d+\. .*""")) -> {
                    val ordered = line.first().isDigit()
                    val items = mutableListOf<ListItem>()

                    while (i < lines.size &&
                        (lines[i].startsWith("- ")
                                || lines[i].startsWith("* ")
                                || lines[i].matches(Regex("""\d+\. .*""")))
                    ) {
                        val content = lines[i]
                            .replace(Regex("""^(\d+\.|- |\* )"""), "")
                            .trim()

                        items += ListItem(
                            id = generateNodeId(),
                            blocks = listOf(
                                Paragraph(
                                    id = generateNodeId(),
                                    inlines = parseInlines(content)
                                )
                            )
                        )
                        i++
                    }

                    blocks += ListBlock(
                        id = generateNodeId(),
                        ordered = ordered,
                        items = items
                    )
                }

                line.startsWith("![") -> {
                    // ![alt](url)
                    val match = Regex("""!\[(.*?)]\((.*?)\)""").find(line)
                    if (match != null) {
                        val resourceUrl = match.groupValues[2]
                        val resourceData = fileService.readBytes(baseFile.resolve(resourceUrl))
                        val resourceId = generateNodeId()
                        resourceRepository.save(resourceId, resourceData)
                        blocks += ImageBlock(
                            id = generateNodeId(),
                            imageRef = ImageRef(
                                id = generateNodeId(),
                                mimeType = guessMimeType(resourceUrl),
                                resourceFileId = resourceId
                            ),
                            caption = parseInlines(match.groupValues[1])
                        )
                    }
                    i++
                }

                else -> {
                    val paragraphLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].isNotBlank()) {
                        paragraphLines += lines[i]
                        i++
                    }

                    blocks += Paragraph(
                        id = generateNodeId(),
                        inlines = parseInlines(paragraphLines.joinToString(" "))
                    )
                }
            }
        }
        return blocks
    }
    private fun parseInlines(text: String): List<InlineNode> {
        val nodes = mutableListOf<InlineNode>()
        var i = 0

        fun readUntil(predicate: (Char) -> Boolean): String {
            val start = i
            while (i < text.length && !predicate(text[i])) i++
            return text.substring(start, i)
        }

        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    i += 2
                    val content = readUntil { text.startsWith("**", i) }
                    i += 2
                    nodes += Emphasized(
                        emphasis = setOf(Emphasis.BOLD),
                        children = parseInlines(content)
                    )
                }

                text.startsWith("*", i) -> {
                    i++
                    val content = readUntil { text[i] == '*' }
                    i++
                    nodes += Emphasized(
                        emphasis = setOf(Emphasis.ITALIC),
                        children = parseInlines(content)
                    )
                }

                text.startsWith("[", i) -> {
                    val endText = text.indexOf("]", i)
                    val endUrl = text.indexOf(")", endText)
                    if (endText > 0 && endUrl > 0 && text[endText + 1] == '(') {
                        val label = text.substring(i + 1, endText)
                        val url = text.substring(endText + 2, endUrl)
                        nodes += Link(
                            target = LinkTarget.External(url),
                            children = parseInlines(label)
                        )
                        i = endUrl + 1
                    } else {
                        nodes += Text(text[i++].toString())
                    }
                }

                else -> {
                    nodes += Text(readUntil { it == '*' || it == '_' || it == '[' })
                }
            }
        }
        return nodes
    }

    private fun extractTitle(blocks: List<BlockNode>): String? =
        blocks.firstOrNull { it is Heading && it.level == 1 }
            ?.let { (it as Heading).inlines.joinToString("") { n ->
                if (n is Text) n.text else ""
            }}

    private fun guessMimeType(url: String): String =
        when {
            url.endsWith(".png") -> "image/png"
            url.endsWith(".jpg") || url.endsWith(".jpeg") -> "image/jpeg"
            url.endsWith(".gif") -> "image/gif"
            else -> "application/octet-stream"
        }
}
