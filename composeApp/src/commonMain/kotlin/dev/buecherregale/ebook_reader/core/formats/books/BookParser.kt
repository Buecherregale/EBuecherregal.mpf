package dev.buecherregale.ebook_reader.core.formats.books

import dev.buecherregale.ebook_reader.core.domain.BookMetadata
import dev.buecherregale.ebook_reader.core.domain.BookType

/**
 * Interface to parse files as books.
 * Implementations should cover different file types.
 *
 *
 * A parser has to be registered via [BookParserFactory.register].
 * Therefore, a parser needs to implement a method to check weather it can parse a given file, although the method does not have to live in the given class itself.
 * Additionally, the BookParserFactory requires a producing method. This method essentially controls which dependencies can be injected (closures can circumvent that though).
 *
 *
 *
 * The Parser workflow is as follows:
 *  * A parser is created via [BookParserFactory.get]
 *  * Book metadata and cover are read to construct a Book
 *  * When a book gets opened, its data is obtained via a [NavigationController] created by [.getNavigationController]
 * There can be invocations of [.getMetadata] without the book ever being opened or read, the actual content only has to be read for the navigation controller
 *
 */
interface BookParser {

    /**
     * Returns the book metadata
     *
     * @return the metadata specified in the book files
     */
    fun metadata(): BookMetadata

    /**
     * gets all the bytes for the cover image, weather its taken from an image file or extracted from a larger file.
     *
     * @return the image bytes, or null if no cover image is found
     */
    fun coverBytes(): ByteArray

    /**
     * Creates a navigation controller that allows readers to access the data
     * needed to render the book.
     * Renderer will only be provided with the navigation controller to display the book.
     *
     * @return the NavigationController implementation
     */
    fun navigationController(): NavigationController

    /**
     * Returns the type of book this parser can parse.
     *
     * @return the parsable book type
     */
    fun parsableType(): BookType
}
