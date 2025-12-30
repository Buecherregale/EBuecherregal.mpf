package dev.buecherregale.ebook_reader.core.formats.books

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef

/**
 * Creates [BookParser] instances.
 * Checks if a given implementation can parse a given file based on [ParseChecker].
 * Creates the instances via the [ParserConstructor].
 *
 */
class BookParserFactory(
    val fileService: FileService
) {
    /**
     * Interface to check if the parser can parse a book.
     */
    fun interface ParseChecker {
        /**
         * Check if the parser, this ParseChecker was registered with, can parse the given book.
         * If true, the parser will be constructed and returned to the user.
         *
         * @param bookFiles the book file(s)
         * @return if the parser can parse the files
         */
        fun canParse(bookFiles: FileRef): Boolean
    }

    /**
     * Two-Arg Constructor for a BookParser implementation.
     * Registered with a [ParseChecker], will construct a new parser if [ParseChecker.canParse] returns true.
     */
    fun interface ParserConstructor {
        /**
         * Constructs a parser with the book files.
         * These are the dependencies a parser can use.
         *
         * @param fileService the file service (obtained from the BookParserFactory instance)
         * @param bookFiles the book file(s)
         * @return a new parser instance
         */
        fun create(fileService: FileService, bookFiles: FileRef): BookParser
    }

    private data class RegisteredParser(val creator: ParserConstructor, val checker: ParseChecker)

    /**
     * Obtains a parser with the bookFiles embedded if a registered implementations` [ParseChecker] returns true.
     * If multiple checkers would return true, the first one is used. An order is not guaranteed, meaning the second
     * parser registered could be returned instead.
     *
     * @param bookFiles the book file(s)
     * @return a BookParser instance
     */
    fun get(bookFiles: FileRef): BookParser {
        for (impl in impls) if (impl.checker.canParse(
                bookFiles
            )
        ) return impl.creator.create(fileService, bookFiles)
        throw IllegalArgumentException("no parser found for book $bookFiles")
    }

    companion object {
        private val impls: MutableList<RegisteredParser> = ArrayList()

        /**
         * Register a new parser implementation.
         * An implementation needs a way of constructing with a single argument, the book files.
         * A method is required to check if the given parser can parse the book format at hand.
         *
         * @param parseChecker a method to check if a parser can parse the format
         * @param constructor  a method to construct the parser implementation
         */
        fun register(parseChecker: ParseChecker, constructor: ParserConstructor) {
            impls.add(
                RegisteredParser(
                    constructor,
                    parseChecker
                )
            )
            Logger.d("registered parser with constructor: $constructor")
        }
    }
}
