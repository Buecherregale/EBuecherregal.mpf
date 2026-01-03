@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.core.repository

import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.BookMetadata
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import dev.buecherregale.sql.Books
import dev.buecherregale.sql.BooksQueries
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** repository for the book files themselves (e.g. epub) */
class BookFileRepository(
    delegate: FileRepository<Uuid>
): FileBasedRepository<Uuid> by delegate

class BookCoverRepository(
    delegate: FileRepository<Uuid>
): FileBasedRepository<Uuid> by delegate

typealias BookRepository = Repository<Uuid, Book>

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