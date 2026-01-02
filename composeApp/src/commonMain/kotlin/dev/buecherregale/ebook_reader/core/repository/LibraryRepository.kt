@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.core.repository

import dev.buecherregale.ebook_reader.core.domain.Library
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import dev.buecherregale.sql.Libraries
import dev.buecherregale.sql.LibrariesQueries
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias LibraryImageRepository = FileRepository<Uuid>

interface LibraryRepository: Repository<Uuid, Library> {
    suspend fun loadByName(name: String): Library?
    suspend fun addBook(libraryId: Uuid, bookId: Uuid)
}


class JsonLibraryRepository(
    fileService: FileService,
    private val jsonUtil: JsonUtil,
    ) : LibraryRepository {

    private val libDir: FileRef = fileService.getAppDirectory(AppDirectory.STATE).resolve("libraries")
    private val delegate: FileRepository<Uuid> = FileRepository(
        keyToFilename = { name -> "$name.json" },
        storeInDir = libDir,
        fileService = fileService
    )

    override suspend fun loadByName(name: String): Library {
        throw UnsupportedOperationException()
    }

    override suspend fun addBook(libraryId: Uuid, bookId: Uuid) {
        throw UnsupportedOperationException()
    }

    override suspend fun loadAll(): List<Library> {
        return delegate.loadAll()
            .map { it.decodeToString() }
            .map { jsonUtil.deserialize<Library>(it) }
            .toList()
    }

    override suspend fun load(key: Uuid): Library? {
        val bytes: ByteArray =  delegate.load(key) ?: return null
        return jsonUtil.deserialize(bytes.decodeToString())
    }

    override suspend fun save(
        key: Uuid,
        value: Library
    ) {
        delegate.save(key, jsonUtil.serialize(value).encodeToByteArray())
    }

    override suspend fun delete(key: Uuid) {
        throw UnsupportedOperationException("cant delete files yet") // TODO: enable file deletion
    }
}

class LibrarySqlRepository(
    private val queries: LibrariesQueries
) : LibraryRepository {

    override suspend fun loadAll(): List<Library> =
        queries.selectAllLibraries()
            .executeAsList()
            .map { row ->
                row.toLibrary(loadBookIds(row.id))
            }

    override suspend fun load(key: Uuid): Library? =
        queries.selectLibraryById(key.toString())
            .executeAsOneOrNull()
            ?.toLibrary(loadBookIds(key.toString()))

    override suspend fun loadByName(name: String): Library? =
        queries.selectLibraryByName(name)
            .executeAsOneOrNull().let { libraries ->
                return@loadByName libraries?.toLibrary(loadBookIds(libraries.id))
            }

    override suspend fun addBook(libraryId: Uuid, bookId: Uuid) {
        queries.insertLibraryBook(libraryId.toString(), bookId.toString())
    }

    override suspend fun save(key: Uuid, value: Library) {
        val exists = queries.selectLibraryById(key.toString())
            .executeAsOneOrNull() != null

        if (exists) {
            queries.updateLibraryName(
                name = value.name,
                id = key.toString()
            )
        } else {
            queries.insertLibrary(
                id = key.toString(),
                name = value.name
            )
        }

        // Replace books
        queries.deleteLibraryBooks(key.toString())
        value.bookIds.forEach { bookId ->
            queries.insertLibraryBook(
                library_id = key.toString(),
                book_id = bookId.toString()
            )
        }
    }

    override suspend fun delete(key: Uuid) {
        queries.deleteLibraryBooks(key.toString())
        queries.deleteLibrary(key.toString())
    }

    private fun loadBookIds(libraryId: String): List<Uuid> =
        queries.selectBookIdsForLibrary(libraryId)
            .executeAsList()
            .map { Uuid.parse(it) }
}

private fun Libraries.toLibrary(bookIds: List<Uuid>): Library =
    Library(
        id = Uuid.parse(id),
        name = name,
        bookIds = bookIds.toMutableList() // TODO: make list immutable
    )


