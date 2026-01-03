@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.core.dom.epub

import dev.buecherregale.ebook_reader.core.dom.ImageRef
import dev.buecherregale.ebook_reader.core.dom.ResourceRepository
import dev.buecherregale.ebook_reader.core.dom.epub.xml.Item
import dev.buecherregale.ebook_reader.core.dom.epub.xml.Package
import dev.buecherregale.ebook_reader.core.exception.EPubParseException
import dev.buecherregale.ebook_reader.core.exception.EPubSyntaxException
import dev.buecherregale.ebook_reader.core.dom.epub.xml.Container
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import kotlinx.io.readByteArray
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML1_0
import nl.adaptivity.xmlutil.serialization.XmlConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    private val resourceRepository: ResourceRepository
) {
    suspend fun extractImage(href: String) : ImageRef {
        val res = extractResource(href)
        return ImageRef(
            id = generateNodeId(),
            mimeType = res.second.mediaType,
            resourceFileId = res.first
        )
    }

    suspend fun extractResource(href: String) : Pair<Uuid, Item> {
        val manifestItem = opf.manifest.items.find { item -> item.href == href }
            ?: throw EPubParseException("could not find resource '$href' in manifest")
        val bytes = (zip.getEntry(href)
            ?: throw EPubParseException("could not find resource '$href' in zip")
                )
            .open().readByteArray()
        val resourceId = Uuid.generateV4()
        resourceRepository.save(resourceId, bytes)
        return resourceId to manifestItem
    }

    fun resolveId(xmlId: String) : Item {
        return opf.manifest.items.find { (itemId, _, _) -> xmlId == itemId }
            ?: throw EPubParseException("could not find item '$xmlId' in manifest")
    }

    fun resolvePath(base: String, href: String) : String {
        val baseParts = base.substringBeforeLast('/', "")
            .split('/')
            .filter { it.isNotEmpty() }
            .toMutableList()
        val refParts = href.split('/')

        for (part in refParts) {
            when (part) {
                "", "." -> Unit
                ".." -> if (baseParts.isNotEmpty()) baseParts.removeAt(baseParts.lastIndex)
                else -> baseParts.add(part)
            }
        }
        return baseParts.joinToString("/")
    }
}
