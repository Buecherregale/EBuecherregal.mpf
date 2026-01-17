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

class DictionaryEntryRepository(
    delegate: FileRepository<Uuid>
): FileBasedRepository<Uuid> by delegate

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
            .map { file ->
                return@map fileService.read(file)
            }
            .map { json ->
                return@map jsonUtil.deserialize<Dictionary>(json)
            }.toList()
    }

    override suspend fun load(key: Uuid): Dictionary? {
        val json = fileService.read(dictionaryFile(key))
        return jsonUtil.deserialize(json)
    }

    override suspend fun save(
        key: Uuid,
        value: Dictionary
    ): Dictionary {
        fileService.write(dictionaryFile(key), jsonUtil.serialize(value))
        return value
    }

    override suspend fun delete(key: Uuid) {
        TODO("Not yet implemented")
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
