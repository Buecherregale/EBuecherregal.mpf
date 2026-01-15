@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.core.repository

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.BookMetadata
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
            language = value.metadata.language.toLanguageTag()
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
            language = Locale(language)
        )
    )
