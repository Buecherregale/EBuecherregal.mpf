package dev.buecherregale.ebook_reader.core.repository

import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface BookRepository: Repository<Uuid, Book> {
}

@OptIn(ExperimentalUuidApi::class)
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