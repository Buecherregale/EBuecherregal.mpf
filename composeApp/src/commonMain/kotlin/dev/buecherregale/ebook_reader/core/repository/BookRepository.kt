@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.core.repository

import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.BookMetadata
import dev.buecherregale.ebook_reader.core.domain.BookType
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import dev.buecherregale.sql.Books
import dev.buecherregale.sql.BooksQueries
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias BookRepository = Repository<Uuid, Book>
typealias BookCoverRepository = FileRepository<Uuid>
/** repository for the book files themselves (e.g. epub) */
typealias BookFileRepository = FileRepository<Uuid>

class BookSqlRepository(
    private val queries: BooksQueries
): BookRepository {
    override suspend fun loadAll(): List<Book> {
        return queries.selectAll().executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun load(key: Uuid): Book? {
        return queries.selectById(key.toString())
            .executeAsOneOrNull()
            .let { it?.toDomain() }
    }

    override suspend fun save(
        key: Uuid,
        value: Book
    ): Book {
        queries.upsert(
            id = key.toString(),
            progress = value.progress,

            book_type = value.bookType.name,

            title = value.metadata.title,
            author = value.metadata.author,
            isbn = value.metadata.isbn,
            language = value.metadata.language
        )
        return value
    }

    override suspend fun delete(key: Uuid) {
        queries.deleteById(key.toString())
    }

}

fun Books.toDomain(): Book =
    Book(
        id = Uuid.parse(id),
        progress = progress,
        bookType = BookType.valueOf(book_type),
        metadata = BookMetadata(
            title = title,
            author = author,
            isbn = isbn,
            language = language
        )
    )


class JsonBookRepository(
    private val fileService: FileService,
    private val jsonUtil: JsonUtil,
) : BookRepository {

    private val bookDir: FileRef = fileService.getAppDirectory(AppDirectory.DATA).resolve("books")

    override suspend fun loadAll(): List<Book> {
        TODO("Not yet implemented")
    }

    override suspend fun load(key: Uuid): Book? {
        val fileContent: String = fileService.read(metaTarget(key))
        return jsonUtil.deserialize(fileContent)
    }

    override suspend fun save(
        key: Uuid,
        value: Book
    ): Book {
        val serializedMetadata: String = jsonUtil.serialize(value)
        fileService.write(metaTarget(key), serializedMetadata)
        return value
    }

    override suspend fun delete(key: Uuid) {
        TODO("Not yet implemented")
    }

    private fun metaTarget(bookId: Uuid?): FileRef = bookDir.resolve("$bookId.meta")

}