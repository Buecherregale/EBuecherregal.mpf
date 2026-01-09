package dev.buecherregale.ebook_reader.core.dom.epub

import dev.buecherregale.ebook_reader.core.dom.ImageRef
import dev.buecherregale.ebook_reader.core.dom.epub.xml.Item
import dev.buecherregale.ebook_reader.core.dom.epub.xml.Package
import dev.buecherregale.ebook_reader.core.exception.EPubParseException
import dev.buecherregale.ebook_reader.core.exception.EPubSyntaxException
import dev.buecherregale.ebook_reader.core.formats.books.epub.EPubConstants
import dev.buecherregale.ebook_reader.core.formats.books.epub.xml.Container
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import kotlinx.io.readByteArray
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML1_0
import nl.adaptivity.xmlutil.serialization.XmlConfig

@OptIn(ExperimentalXmlUtilApi::class)
private val xmlParser = XML1_0.recommended {
    policy = DefaultXmlSerializationPolicy {
        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
    }
}

internal object ContainerReader {
     suspend fun read(zip: ZipFileRef): Container {
        val xmlEntry = zip.getEntry(EPubConstants.CONTAINER_XML)
            ?: throw EPubSyntaxException("${EPubConstants.CONTAINER_XML} is missing")

        val xmlString = xmlEntry.open().use {
            it.readByteArray().decodeToString()
        }

        val container = xmlParser.decodeFromString<Container>(xmlString)

        if (container.rootfiles.rootfile.isEmpty()) {
            throw EPubSyntaxException("no rootfiles in container.xml")
        }

        return container
    }
}

internal object PackageReader {
    suspend fun read(zip: ZipFileRef, opfPath: String): Package {
        val xmlEntry = zip.getEntry(opfPath)
            ?: throw EPubParseException("rootfile $opfPath not found")

        val xmlString = xmlEntry.open().use {
            it.readByteArray().decodeToString()
        }

        return xmlParser.decodeFromString(xmlString)
    }
}

internal class ResourceResolver(
    private val zip: ZipFileRef,
    private val opf: Package,
) {
    suspend fun resolveImage(href: String): ImageRef {
        val manifestItem = opf.manifest.items.find { item -> item.href == href }
            ?: throw EPubParseException("could not find resource '$href' in manifest")
        val bytes = (zip.getEntry(href)
            ?: throw EPubParseException("could not find resource '$href' in zip")
                )
            .open().readByteArray()
        return ImageRef(manifestItem.id, manifestItem.mediaType, bytes)
    }

    fun resolveId(id: String) : Item {
        return opf.manifest.items.find { (itemId, _, _) -> itemId == itemId }
            ?: throw EPubParseException("could not find item '$id' in manifest")
    }
}
