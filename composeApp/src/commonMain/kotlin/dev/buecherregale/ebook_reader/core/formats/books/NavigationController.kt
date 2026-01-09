package dev.buecherregale.ebook_reader.core.formats.books

/**
 * A NavigationController allows to navigate a book by scrolling forward or backwards.
 * What exactly is scrolled, is up to the implementation. <br></br>
 * For example: EPubs might scroll chapters, while PDFs might scroll pages.
 * <br></br>
 * The NavigationController needs to be enough to obtain the data for the book needed to render it. <br></br>
 * It follows that knowing the implementation of the controller should be enough to render the book.
 * <br></br>
 * TODO: this class still needs `instanceof` checks. maybe an interface for [dev.buecherregale.ebuecherregale.core.books.epub.EPubNavigationElement]?
 */
@Deprecated("replaced by unified dom, see dev.buecherregale.ebook_reader.core.dom.NavigationController.kt")
interface NavigationController {
    /**
     * Gets the next scrollable item.
     * If the reader reaches its end, the result is null.
     *
     * @return the next item or null
     */
    fun next(): Any?

    /**
     * Gets the previous scrollable item.
     * If the controller reaches the beginning of the book, further calls result in a null result.
     *
     * @return the previous item or null
     */
    fun prev(): Any?

    /**
     * Gets the page at the desired progress.
     *
     * @param progress number between 0 and 1 indicating the progress in the book
     * @return the item at that part of the book
     */
    fun get(progress: Double): Any

    /**
     * Returns how far the navigation controller has scrolled within the book.
     *
     * @return the progress between 0 and 1
     */
    fun progress(): Double
}
