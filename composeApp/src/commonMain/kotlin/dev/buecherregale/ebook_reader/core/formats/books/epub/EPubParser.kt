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
class EPubParser private constructor(
    private val zip: ZipFileRef,
    private val content: Package,
    private val manifestMap: Map<String, EPubResource>
) : BookParser {

    override fun parsableType(): BookType = BookType.EPUB

    override suspend  fun metadata(): BookMetadata {
        val metadata = content.metadata
        val isbn = metadata.identifiers
            .find { it.scheme == EPubConstants.PREFERRED_IDENTIFIER_SCHEME }
            ?.value
            ?: ""

        return BookMetadata(
            title = metadata.title,
            author = metadata.creator.first().name,
            isbn = isbn,
            language = metadata.language
        )
    }

    override suspend fun coverBytes(): ByteArray {
        val coverEntryPath = manifestMap[EPubConstants.COVER_XML_ID]?.href
            ?: throw EPubParseException("no cover file with xml id '${EPubConstants.COVER_XML_ID}'")

        val coverEntry = zip.getEntry(coverEntryPath)
            ?: throw EPubParseException("could not read cover file at path '$coverEntryPath'")

        return coverEntry.open().use { it.readByteArray() }
    }

    override suspend fun navigationController(): NavigationController =
        SpineManager(zip, manifestMap, content.spine)

    companion object {

        /**
         * Suspending factory replacing the old constructor logic
         */
        suspend fun create(
            fileService: FileService,
            bookFiles: FileRef
        ): EPubParser {
            val zip = fileService.readZip(bookFiles)
            val container = parseContainerXml(zip)
            val content = parseRootfile(zip, container.rootfiles.rootfile[0])
            val manifestMap = content.manifest.toMap()

            return EPubParser(zip, content, manifestMap)
        }

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
        suspend fun isEPub(
            fileService: FileService,
            bookFiles: FileRef
        ): Boolean {
            fileService.readZip(bookFiles).use { zip ->
                val mimetypeEntry = zip.getEntry(EPubConstants.MIMETYPE) ?: return false
                return mimetypeEntry
                    .open()
                    .use { it.readByteArray().decodeToString() } == EPubConstants.CONTENT_TYPE
            }
        }

        private suspend fun parseContainerXml(zip: ZipFileRef): Container {
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

        private suspend fun parseRootfile(
            zip: ZipFileRef,
            rootfile: RootFile
        ): Package {
            val xmlEntry = zip.getEntry(rootfile.fullPath)
                ?: throw EPubParseException("rootfile $rootfile not found")

            val xmlString = xmlEntry.open().use {
                it.readByteArray().decodeToString()
            }

            return xmlParser.decodeFromString(xmlString)
        }

        @OptIn(ExperimentalXmlUtilApi::class)
        private val xmlParser = XML1_0.recommended {
            policy = DefaultXmlSerializationPolicy {
                unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }

        private fun Manifest.toMap(): Map<String, EPubResource> =
            items.associate {
                it.id to EPubResource(it.id, it.href, it.mediaType)
            }
    }
}
