package dev.buecherregale.ebook_reader.core.dom.epub

import dev.buecherregale.ebook_reader.core.dom.*
import dev.buecherregale.ebook_reader.core.dom.epub.xml.*
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.BookMetadata
import dev.buecherregale.ebook_reader.core.exception.EPubParseException
import dev.buecherregale.ebook_reader.core.language.normalizeLanguage
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import kotlinx.io.readByteArray
import kotlinx.io.readString
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.attributes
import nl.adaptivity.xmlutil.localPart
import nl.adaptivity.xmlutil.xmlStreaming
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class EPubParser : DocumentParser {

    override val format = DocumentFormat.EPUB

    override suspend fun parse(
        targetId: Uuid,
        file: FileRef,
        fileService: FileService,
        resourceRepository: ResourceRepository
    ): Pair<Book, DomDocument> {
        val zip = fileService.readZip(file)
        val container = ContainerReader.read(zip)
        val opf = PackageReader.read(zip, container.rootfiles.rootfile[0].fullPath)
        val resources = ResourceResolver(zip, opf, resourceRepository)
        val chapters = opf.spine.itemRefs
            .map { resources.resolveId(it.idref) }
            .map { convert(
            zip = zip,
            item = it,
            resources = resources
        )}
        return Book(
            id = targetId,
            progress = 0.0,
            metadata = extractMetadata(opf)
        ) to DomDocument(
            schemaVersion = DOM_SCHEMA_VERSION,
            chapter = chapters
        )
    }

    override suspend fun getCoverBytes(
        file: FileRef,
        fileService: FileService
    ): ByteArray? {
        val zip = fileService.readZip(file)
        val container = ContainerReader.read(zip)
        val opf = PackageReader.read(zip, container.rootfiles.rootfile[0].fullPath)

        return opf.manifest.items
            .filter { (id, _, _) -> id == EPubConstants.COVER_XML_ID }
            .mapNotNull { item -> zip.getEntry(item.href) }
            .map { it.open().readByteArray() }
            .firstOrNull()
    }

    private fun createHandlers(): TagHandlerRegistry =
        TagHandlerRegistry().apply {
            register("p", ParagraphHandler())

            register("h1", HeadingHandler(1))
            register("h2", HeadingHandler(2))
            register("h3", HeadingHandler(3))
            register("h4", HeadingHandler(4))
            register("h5", HeadingHandler(5))
            register("h6", HeadingHandler(6))

            register("strong", EmphasisHandler(Emphasis.BOLD))
            register("b", EmphasisHandler(Emphasis.BOLD))
            register("em", EmphasisHandler(Emphasis.ITALIC))
            register("i", EmphasisHandler(Emphasis.ITALIC))

            register("image", ImageHandler())
            register("img", ImageHandler())

            register("ruby", RubyHandler())
            register("rt", RubyTextHandler())
            register("rp", RubyParenthesesHandler())
            register("rb", RubyBaseHandler())
        }

    /**
     * Maybe use more dynamic parser instead
     */
    private fun extractMetadata(opf: Package) : BookMetadata {
        val isbn = opf.metadata.identifiers
            .find { it.scheme == EPubConstants.PREFERRED_IDENTIFIER_SCHEME }
            ?.value
            ?: ""
        return BookMetadata(
            title = opf.metadata.title,
            language = normalizeLanguage(opf.metadata.language),
            author = opf.metadata.creator.first().name,
            isbn = isbn
        )
    }

    private suspend fun convert(
        zip: ZipFileRef,
        item: Item,
        resources: ResourceResolver
    ): Chapter {
        val entry = zip.getEntry(item.href)
            ?: throw EPubParseException("Missing item ${item.id}")

        val handlers = createHandlers()
        val blocks = mutableListOf<BlockNode>()
        val ctx = ParseContext(item, blocks, resources)

        val reader = xmlStreaming.newReader(entry.open().readString())
        while (reader.hasNext()) {
            when (reader.next()) {
                EventType.START_ELEMENT -> {
                    handlers.get(reader.name.localPart)
                        ?.onStart(ctx, reader.attributes.associate { it.name.localPart to it.value })
                }

                EventType.TEXT -> {
                    if (reader.isWhitespace()) continue
                    ctx.inlineStack.addText(reader.text)
                }

                EventType.END_ELEMENT -> {
                    handlers.get(reader.name.localPart)
                        ?.onEnd(ctx)
                }

                else -> {}
            }
        }

        return Chapter(
            id = item.id,
            title = item.id,
            blocks = blocks
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
internal fun generateNodeId(): String {
    return Uuid.generateV4().toString()
}
