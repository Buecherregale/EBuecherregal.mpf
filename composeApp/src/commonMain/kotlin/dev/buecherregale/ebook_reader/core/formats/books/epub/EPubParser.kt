package dev.buecherregale.ebook_reader.core.formats.books.epub

import dev.buecherregale.ebook_reader.core.formats.books.BookParser
import dev.buecherregale.ebook_reader.core.formats.books.NavigationController
import dev.buecherregale.ebook_reader.core.domain.BookMetadata
import dev.buecherregale.ebook_reader.core.domain.BookType
import dev.buecherregale.ebook_reader.core.exception.EPubParseException
import dev.buecherregale.ebook_reader.core.exception.EPubSyntaxException
import dev.buecherregale.ebook_reader.core.formats.books.epub.xml.Container
import dev.buecherregale.ebook_reader.core.formats.books.epub.xml.Manifest
import dev.buecherregale.ebook_reader.core.formats.books.epub.xml.Package
import dev.buecherregale.ebook_reader.core.formats.books.epub.xml.RootFile
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import kotlinx.io.readByteArray
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML1_0
import nl.adaptivity.xmlutil.serialization.XmlConfig

/**
 *
 *
 * A parser for the epub format for books.
 * An epub is essentially a zip file containing one or more root files described in a container.xml file.
 * This parser will only read ONE SINGLE root file.
 * <br></br>
 * Additionally, this implementation provides the required checker via [.canParse].
 * <br></br>
 * The parser does not write to files anywhere, only reading from them.
 * <br></br>
 * Resources and content is read upon request from the [SpineManager] implementation.
 *
 */
class EPubParser(fileService: FileService, bookFiles: FileRef) : BookParser {
    private val zip: ZipFileRef
    private val container: Container
    private val content: Package
    private val manifestMap: Map<String, EPubResource>

    init {
        val zip: ZipFileRef = fileService.readZip(bookFiles)
        this.zip = zip
        this.container = parseContainerXml()
        this.content = parseRootfile(container.rootfiles.rootfile[0])
        this.manifestMap = content.manifest.toMap()
    }

    override fun parsableType(): BookType {
        return BookType.EPUB
    }

    override fun metadata(): BookMetadata {
        val metadata = content.metadata
        val isbn: String = metadata.identifiers.find { identifier -> identifier.scheme == EPubConstants.PREFERRED_IDENTIFIER_SCHEME }?.value ?: ""
        return BookMetadata(
            title = metadata.title,
            author = metadata.creator.first().name,
            isbn = isbn,
            language = metadata.language
        )
    }

    /**
     * Currently fails if no cover with element id [EPubConstants.COVER_XML_ID] is provided.
     * Can this happen?
     */
    override fun coverBytes(): ByteArray {
        val coverEntryPath = manifestMap[EPubConstants.COVER_XML_ID]?.href
            ?: throw EPubParseException("no cover file with xml id '${EPubConstants.COVER_XML_ID}'")
        val coverEntry = zip.getEntry(coverEntryPath) ?: throw EPubParseException("could not read cover file at path '$coverEntryPath'")
        return coverEntry.open().use { source -> source.readByteArray() }
    }

    override fun navigationController(): NavigationController {
        return SpineManager(zip, manifestMap, content.spine)
    }

    /**
     * Parses the xml at [EPubConstants.CONTAINER_XML] to the struct.
     * The file has to be present to be considered a valid epub.
     */
    private fun parseContainerXml(): Container {
        val xmlEntry = zip.getEntry(EPubConstants.CONTAINER_XML) ?: throw EPubSyntaxException(
            EPubConstants.CONTAINER_XML + " is missing")
        val xmlString = xmlEntry.open().use { source ->
            source.readByteArray().decodeToString()
        }
        val c = xmlParser.decodeFromString<Container>(xmlString)
        if (c.rootfiles.rootfile.isEmpty())
            throw EPubSyntaxException("no rootfiles in container.xml")
        return c
    }

    /**
     * Parse the rootfile to a package dto. This normally is the `content.opf`-file.
     */

    private fun parseRootfile(rootfile: RootFile): Package {
        val xmlEntry = zip.getEntry(rootfile.fullPath) ?: throw EPubParseException(
            "rootfile $rootfile not found")
        val xmlString = xmlEntry.open().use { source -> source.readByteArray().decodeToString() }
        return xmlParser.decodeFromString(xmlString)
    }

    companion object {
        /**
         * Checks if the given file is an epub file.
         *
         * To be an epub, the file has to follow:
         * 1. be a `zip`
         * 2. have an entry by name [EPubConstants.MIMETYPE]
         * 3. the entry must ONLY have the content [EPubConstants.CONTENT_TYPE]
         * 4. the [EPubConstants.MIMETYPE] entry has to be the first entry in the zip. However, this is NOT checked by this method.
         *
         * @param fileService the file service to open the zip with
         * @param bookFiles the reference to the candidate
         */
        fun isEPub(fileService: FileService, bookFiles: FileRef): Boolean {
            fileService.readZip(bookFiles).use { zip ->
                val mimetypeEntry = zip.getEntry(EPubConstants.MIMETYPE) ?: return false
                return mimetypeEntry.open().readByteArray().decodeToString() == EPubConstants.CONTENT_TYPE
            }
        }

        @OptIn(ExperimentalXmlUtilApi::class)
        private val xmlParser = XML1_0.recommended {
            policy = DefaultXmlSerializationPolicy {
                unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }

        private fun Manifest.toMap(): Map<String, EPubResource> {
            val map = HashMap<String, EPubResource>()
            for (i in items) {
                map[i.id] = EPubResource(
                    i.id,
                    i.href,
                    i.mediaType
                )
            }
            return map
        }
    }
}
