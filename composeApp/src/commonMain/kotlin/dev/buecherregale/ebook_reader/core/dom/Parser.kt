package dev.buecherregale.ebook_reader.core.dom

import dev.buecherregale.ebook_reader.core.dom.epub.EPubFormatDetector
import dev.buecherregale.ebook_reader.core.dom.epub.EPubParser
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.repository.FileBasedRepository
import dev.buecherregale.ebook_reader.core.repository.FileRepository
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object ParserFactory {
    private val detectors: List<FormatDetector> = listOf(EPubFormatDetector())
    private val parser: Map<DocumentFormat, DocumentParser> = mapOf(
        DocumentFormat.EPUB to EPubParser()
    )

    suspend fun get(
        file: FileRef,
        fileService: FileService
    ): DocumentParser {
        val format = detectors.firstNotNullOfOrNull { it.detect(file, fileService) }
        if (format != null)
            return parser[format] ?: throw IllegalArgumentException("no parser found for format '$format'")
        throw IllegalArgumentException("no detector found for file: '$file'")
    }
}

enum class DocumentFormat {
    EPUB,
    PDF
}

/**
 * Detects the format of a document.
 *
 * A format detector is used in [ParserFactory], where new implementations can be added.
 *
 * A detector may detect one or several formats. For example, there could be a `EPubDetector`, detecting if the
 * given file is an epub, alternatively there could be a `FileExtensionDetector`, detecting `.txt` or `.md`
 * formats.
 */
interface FormatDetector {
    /**
     * Detects the document format.
     *
     * Generally, this method does not need to be called directly, instead is used in [ParserFactory.get] to determine a fitting parser.
     *
     * @param file the document file
     * @param fileService the service to open the file
     *
     * @return the document format or `null` if no supported format is detected
     */
    suspend fun detect(
        file: FileRef,
        fileService: FileService
    ): DocumentFormat?
}


/**
 * A parser converts books of a single document type (see [.format]) to the internal DOM structure.
 * Additionally, it can try to obtain a fitting cover image.
 * The concrete implementation should be obtained via [ParserFactory], which will find the appropriate parser.
 */
@OptIn(ExperimentalUuidApi::class)
interface DocumentParser {

    val format: DocumentFormat

    /**
     * Convert the given file to the DOM structure.
     * Additional resources, like images, should be saved to the ResourceRepository with links/references as nodes if appropriate.
     *
     * @param targetId the id the book should have (needed to provide the correct repository)
     * @param file the book file
     * @param fileService the platform dependent FileService to open/read the files
     * @param resourceRepository the repository to store additional resources to
     *
     * @return the parsed book information and the resulting DOM structure
     */
    suspend fun parse(
        targetId: Uuid,
        file: FileRef,
        fileService: FileService,
        resourceRepository: ResourceRepository
    ) : Pair<Book, DomDocument>

    /**
     * Tries to obtain the bytes of a cover image for the given book.
     *
     * If a cover really exists depends on the format, thus this method may return `null`
     * if the given format does not have covers (e.g. MD) or the cover is missing from the files.
     *
     * @param file the book file
     * @param fileService the platform dependent FileService to read the files
     *
     * @return the bytes for a cover, or `null`
     */
    suspend fun getCoverBytes(file: FileRef,
                         fileService: FileService) : ByteArray?
}

@OptIn(ExperimentalUuidApi::class)
class ResourceRepository(
    delegate: FileRepository<Uuid>
) : FileBasedRepository<Uuid> by delegate