@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.core.repository

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.domain.DictionaryMetadata
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import dev.buecherregale.sql.Dictionaries
import dev.buecherregale.sql.DictionariesQueries
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface DictionaryMetadataRepository : Repository<Uuid, DictionaryMetadata>

fun Dictionaries.toDomain(): DictionaryMetadata =
    DictionaryMetadata(
        id = Uuid.parse(id),
        name = name,
        originalLanguage = Locale(originalLanguage),
        targetLanguage = Locale(targetLanguage)
    )

class DictionarySqlRepository(
    private val queries: DictionariesQueries
): DictionaryMetadataRepository {
    override suspend fun loadAll(): List<DictionaryMetadata> {
        return queries.selectAll().executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun load(key: Uuid): DictionaryMetadata? {
        return queries.selectById(key.toString())
            .executeAsOneOrNull()
            .let { it?.toDomain() }
    }

    override suspend fun save(
        key: Uuid,
        value: DictionaryMetadata
    ): DictionaryMetadata {
        queries.save(
            id = key.toString(),
            name = value.name,
            originalLanguage = value.originalLanguage.toLanguageTag(),
            targetLanguage = value.targetLanguage.toLanguageTag()
        )
        return value
    }

    override suspend fun delete(key: Uuid) {
        queries.deleteById(key.toString())
    }

}

class DictionaryRepository(
    private val fileService: FileService,
    private val jsonUtil: JsonUtil,
): Repository<Uuid, Dictionary> {

    override suspend fun loadAll(): List<Dictionary> {
        return fileService.listChildren(dictionaryDir)
            .mapNotNull { file ->
                if (!fileService.exists(file)) return@mapNotNull null
                val source = fileService.open(file)
                try {
                    jsonUtil.deserialize<Dictionary>(source)
                } catch (_: Exception) {
                    null
                } finally {
                    source.close()
                }
            }
    }

    override suspend fun load(key: Uuid): Dictionary? {
        val file = dictionaryFile(key)
        if (!fileService.exists(file)) return null
        val source = fileService.open(file)
        try {
            return jsonUtil.deserialize(source)
        } finally {
            source.close()
        }
    }

    override suspend fun save(
        key: Uuid,
        value: Dictionary
    ): Dictionary {
        val file = dictionaryFile(key)
        val sink = fileService.openSink(file)
        try {
            jsonUtil.serialize(value, sink)
        } finally {
            sink.close()
        }
        return value
    }

    override suspend fun delete(key: Uuid) {
        fileService.delete(dictionaryFile(key))
    }

    private val dictionaryDir: FileRef = fileService.getAppDirectory(AppDirectory.DATA).resolve("dictionaries")

    /**
     * A file ref to the file containing the dictionary as json. <br></br>
     * <bold>DOES NOT CHECK IF THE FILE EXISTS!</bold> <br></br>
     * If no dictionary with the given id has been downloaded, the file won't exist, but this method <bold>WILL</bold> return
     * the theoretical ref. <br></br>
     * The file name will be `dictId.json`.
     *
     * @param key the id of the dictionary
     * @return file ref to the metadata file
     */
    private fun dictionaryFile(key: Uuid): FileRef = dictionaryDir.resolve("$key.json")
}
