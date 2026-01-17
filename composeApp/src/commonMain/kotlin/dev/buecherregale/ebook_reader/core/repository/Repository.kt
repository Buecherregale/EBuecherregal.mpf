package dev.buecherregale.ebook_reader.core.repository

import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil

/**
 * A generic repository aimed at persisting data.
 *
 * @param Key the type of unique key to access/save data by
 * @param T the type to persist
 */
interface Repository<Key, T> {
    /**
     * Loads all data from the repository.
     */
    suspend fun loadAll(): List<T>

    /**
     * Loads data with the given key.
     */
    suspend fun load(key: Key): T?

    /**
     * Saves data at the given key.
     */
    suspend fun save(key: Key, value: T): T

    /**
     * Deletes data with the given key.
     */
    suspend fun delete(key: Key)
}

interface FileBasedRepository<Key>: Repository<Key, ByteArray> {
    fun getFile(key: Key): FileRef
}


class FileRepository<Key>(
    private val keyToFilename: (Key) -> String,
    private val storeInDir: FileRef,
    private val fileService: FileService,
) : FileBasedRepository<Key> {

    override suspend fun loadAll(): List<ByteArray> {
        return fileService.listChildren(storeInDir)
            .map { fileService.readBytes(it) }
    }

    override suspend fun load(key: Key): ByteArray? {
        val file = storeInDir.resolve(keyToFilename(key))
        if (!fileService.exists(file)) return null

        return fileService.readBytes(file)
    }

    override suspend fun save(key: Key, value: ByteArray): ByteArray {
        val file = storeInDir.resolve(keyToFilename(key))

        fileService.write(file, value)
        return load(key)!! // should def exist now
    }

    override suspend fun delete(key: Key) {
        fileService.delete(storeInDir.resolve(keyToFilename(key)))
    }

    override fun getFile(key: Key): FileRef = storeInDir.resolve(keyToFilename(key))
}
