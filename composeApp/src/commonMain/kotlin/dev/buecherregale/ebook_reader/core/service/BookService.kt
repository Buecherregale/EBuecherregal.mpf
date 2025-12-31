package dev.buecherregale.ebook_reader.core.service

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.formats.books.BookParser
import dev.buecherregale.ebook_reader.core.formats.books.BookParserFactory
import dev.buecherregale.ebook_reader.core.formats.books.NavigationController
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.BookMetadata
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * External api to interact with the books with.
 * Handles file storage, book import and opening a book from a library.
 */
@OptIn(ExperimentalUuidApi::class)
class BookService(private val fileService: FileService,
                  private val jsonUtil: JsonUtil,
                  private val parserFactory: BookParserFactory) {
    private val bookDir: FileRef = fileService.getAppDirectory(AppDirectory.DATA).resolve("books")

    /**
     * Import the book into the app system.
     * Copies the book files to the app files.
     * Extracts a cover based on the parser and saves this in an additional file.
     * Creates a meta file for the book json data.
     *
     * @param bookFiles the book file(s)
     * @return the book instance with metadata
     */
    fun importBook(bookFiles: FileRef): Book {
        Logger.i("importing book from '$bookFiles'")
        val bookId: Uuid = Uuid.generateV4()
        // todo: only copy when parser exists, no merit elsewise, possibly just delete file when no parser exists (new method for fileService)
        val bookTarget: FileRef = copyBook(bookId, bookFiles)

        val parser: BookParser = parserFactory.get(bookTarget)

        val bookMetadata: BookMetadata = parser.metadata()

        val coverBytes: ByteArray = parser.coverBytes()
        saveCover(bookId, coverBytes)

        // after updating the cover path, save the data to file
        val book = Book(
            bookId,
            0.0,
            parser.parsableType(),
            bookMetadata)
        saveBookData(book)

        Logger.i("imported book '${book.metadata.title}' with id ${book.id}")
        return book
    }

    /**
     * Reads the data file for a book (created by [.saveBookData]).
     * Obtains the file path via [.getBookDataFile]
     *
     * @param bookId the id of the book
     * @return the book instance
     */
    fun readData(bookId: Uuid): Book {
        val fileContent: String = fileService.read(getBookDataFile(bookId))
        return jsonUtil.deserialize(fileContent)
    }

    /**
     * Creates the navigation controller to navigate the book
     * and obtain its content. <br></br>
     * This will open and read the book files.
     *
     * @param bookId the id of the book
     * @return a navigation controller implementation based on the used parser
     */
    fun open(bookId: Uuid): NavigationController {
        return parserFactory
            .get(getBookFile(bookId))
            .navigationController()
    }

    /**
     * A file ref to the file containing book data as json. <br></br>
     * BOOK DATA is the serialization of the [Book] type and its metadata. <br></br>
     * <bold>DOES NOT CHECK IF THE FILE EXISTS</bold> <br></br>
     * If no book with the given id has been imported, the file won't exist, but this method <bold>WILL</bold> return
     * the theoretical ref.
     *
     * @param bookId the id of the book
     * @return file ref to the metadata file
     */
    fun getBookDataFile(bookId: Uuid): FileRef {
        return bookDir.resolve("$bookId.meta")
    }

    /**
     * A file ref to the file containing the cover. <br></br>
     * <bold>DOES NOT CHECK IF THE FILE EXISTS</bold> <br></br>
     * If no book with the given id has been imported, the file won't exist, but this method <bold>WILL</bold> return
     * the theoretical ref.
     *
     * @param bookId the id of the book
     * @return file ref to the cover file (image, type unknown)
     */
    fun getCoverFile(bookId: Uuid): FileRef {
        return bookDir.resolve("$bookId.cover")
    }

    /**
     * A file ref to the file containing the book, in whichever format it was imported. <br></br>
     * <bold>DOES NOT CHECK IF THE FILE EXISTS</bold> <br></br>
     * If no book with the given id has been imported, the file won't exist, but this method <bold>WILL</bold> return
     * the theoretical ref.
     *
     * @param bookId the id of the book
     * @return file ref to the book file (original extension (e.g. epub) is NOT preserved
     */
    fun getBookFile(bookId: Uuid?): FileRef {
        return bookDir.resolve("$bookId.book")
    }

    /**
     * Updates the book progress. <br></br>
     * <bold>WRITES THE BOOK DATA again</bold> <br></br>
     * Use this method instead of manipulating the [Book] class.
     *
     * @param book the book to update
     * @param newProgress the new progress value
     *
     * @return the updated book
     */
    fun updateProgress(book: Book, newProgress: Double): Book {
        val v2: Book = Book(book.id,
            newProgress,
            book.bookType,
            book.metadata,)

        saveBookData(v2)
        return v2
    }

    /**
     * Saves the book data to a json file. <br></br>
     * This will not only save the [Book.metadata] but the whole book itself, e.g. the progress.
     * The file name will be [.getBookDataFile].
     * <br></br>
     * This method is public to allow for updated books to be saved, e.g. new [Book.progress].
     *
     * @param book the book data to save
     */
    fun saveBookData(book: Book) {
        val metaTarget: FileRef = getBookDataFile(book.id)
        Logger.d("saving book data for ${book.id} to '$metaTarget'")
        val serializedMetadata: String = jsonUtil.serialize(book)
        fileService.write(metaTarget, serializedMetadata)
    }

    /**
     * Saves the book to the book directory, using the id as file name.
     * The file name will be [.getBookFile].
     *
     * @param bookId the book id to use
     * @param bookFiles the files to copy
     * @return the target where the book has been copied to
     */
    private fun copyBook(bookId: Uuid, bookFiles: FileRef): FileRef {
        val bookTarget = getBookFile(bookId)
        fileService.open(bookFiles).use { input ->
            fileService.copy(input, bookTarget)
            return bookTarget
        }
    }

    /**
     * Save the cover to the book dir by the book id.
     * The cover is not associated with any file type, opting for generic extension `.cover`.
     * The file name will be [.getCoverFile].
     * <br></br>
     * TODO: possibly give filetype (easy for epub via content-type). For that [BookParser.coverBytes] needs to return the filetype
     *
     * @param bookId     the id of the book (used as filename)
     * @param coverBytes the bytes of the cover image to write
     */
    private fun saveCover(bookId: Uuid, coverBytes: ByteArray) {
        val coverTarget = getCoverFile(bookId)
        fileService.write(coverTarget, coverBytes)
    }
}
