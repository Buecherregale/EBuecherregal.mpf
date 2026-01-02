package dev.buecherregale.ebook_reader.core.repository

import dev.buecherregale.ebook_reader.core.domain.Library
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil

interface LibraryRepository: Repository<String, Library> {
    suspend fun saveImage(key: String, imageBytes: ByteArray)
    suspend fun readImage(key: String): ByteArray?
}


class JsonLibraryRepository(
    private val fileService: FileService,
    private val jsonUtil: JsonUtil,
) : LibraryRepository {

    private val libDir: FileRef = fileService.getAppDirectory(AppDirectory.STATE).resolve("libraries")

    override suspend fun loadAll(): List<Library> {
        return fileService.listChildren(libDir)
            .filter { ref -> !fileService.getMetadata(ref).isDirectory }
            .map {ref  -> fileService.read(ref) }
            .map(jsonUtil::deserialize)
    }

    override suspend fun load(key: String): Library? {
        val content: String = fileService.read(fileRef(key))
        return jsonUtil.deserialize(content)
    }

    override suspend fun save(
        key: String,
        value: Library
    ) {
        fileService.write(fileRef(key), jsonUtil.serialize(value))
    }

    override suspend fun delete(key: String) {
        TODO("Not yet implemented")
    }

    override suspend fun saveImage(key: String, imageBytes: ByteArray) {
        fileService.write(
            file = imageFileRef(key),
            imageBytes
        )
    }

    override suspend fun readImage(key: String): ByteArray? {
        val target = imageFileRef(key)
        if (!fileService.exists(target)) return null
        return fileService.readBytes(target)
    }

    private fun imageFileRef(name: String): FileRef = libDir.resolve("images").resolve(name)
    private fun fileRef(name: String): FileRef = libDir.resolve("$name.json")
}