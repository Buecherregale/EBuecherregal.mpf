package dev.buecherregale.ebook_reader.core.dom.epub

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.dom.*
import dev.buecherregale.ebook_reader.core.dom.epub.xml.Item
import dev.buecherregale.ebook_reader.core.exception.EPubParseException
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import it.calogerosanfilippo.aral.xml.XMLParserEvent
import it.calogerosanfilippo.aral.xml.XMLParserFactory
import kotlinx.io.readString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class EPubParser : DocumentParser {

    override val format = DocumentFormat.EPUB

    override suspend fun parse(file: FileRef, fileService: FileService): DomDocument {
        val zip = fileService.readZip(file)
        val container = ContainerReader.read(zip)
        val opf = PackageReader.read(zip, container.rootfiles.rootfile[0].fullPath)
        val resources = ResourceResolver(zip, opf)
        val chapters = opf.spine.itemRefs
            .map {
                resources.resolveId(it.idref)
            }
            .map { convert(
            zip = zip,
            item = it,
            resources = ResourceResolver(zip, opf)
        )}
        return DomDocument(
            schemaVersion = DOM_SCHEMA_VERSION,
            chapter = chapters
        )
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

            register("img", ImageHandler())
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
        val ctx = ParseContext(blocks, resources)

        XMLParserFactory.getParser()
            .parse(entry.open().readString())
            .collect { event ->
                when (event) {
                    is XMLParserEvent.ElementStartFound -> {
                        handlers.get(event.name)
                            ?.onStart(ctx, event.attributes)
                    }
                    is XMLParserEvent.CharactersFound -> {
                        ctx.inlineStack.addText(event.characters)
                    }
                    is XMLParserEvent.ElementEndFound -> {
                        handlers.get(event.name)
                            ?.onEnd(ctx)
                    }
                    is XMLParserEvent.Error -> {
                        Logger.w { "error parsing item ${item.id}: ${event.exception}" }
                    }
                    else -> { }
                }
            }

        return Chapter(
            id = item.id,
            title = null,
            blocks = blocks
        )
    }


}

@OptIn(ExperimentalUuidApi::class)
internal fun generateNodeId(): String {
    return Uuid.generateV4().toString()
}
