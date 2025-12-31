package dev.buecherregale.ebook_reader.core.service

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.domain.DictionaryEntry
import dev.buecherregale.ebook_reader.core.domain.DictionaryMetadata
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Service to download and manage downloaded dictionaries. When downloading a dictionary via [.download],
 * the dictionary is transformed into a standard format, the [Dictionary] dto and saved like that.
 * <br></br>
 * Supported dictionaries need to have a [dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporter] implemented and registered
 * in the [DictionaryImporterFactory]. <br></br>
 * Generally this class will write and read dictionaries from [.dictionaryDir], deleting or modifying files therein may result in errors.
 */
@OptIn(ExperimentalUuidApi::class)
class DictionaryService constructor(
    private val fileService: FileService,
    private val jsonUtil: JsonUtil,
    private val importerFactory: DictionaryImporterFactory,
) {
    private val dictionaryDir: FileRef = fileService.getAppDirectory(AppDirectory.DATA).resolve("dictionaries")

    /**
     * Downloads the dictionary, transforming it into the [dev.buecherregale.ebook_reader.core.domain.Dictionary] form and saving it to [.getDictionaryFile].
     * <br></br>
     * This happens by calling [dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporter.download]. <br></br>
     * Then its serialized as a json file to [.getDictionaryFile].
     *
     * @param dictionaryName the dictionary name
     * @param language the target language
     * @return the downloaded dictionary
     */
    suspend fun download(dictionaryName: String, language: String): Dictionary {
        Logger.i("downloading dictionary '$dictionaryName' in '$language'")
        val downloaded: Dictionary = importerFactory
            .forName(dictionaryName)
            .download(language)
        save(downloaded)
        return downloaded
    }

    /**
     * Opens an existing dictionary file, deserializing its content. <br></br>
     * Expects the dictionary at the [.getDictionaryFile] file.
     *
     * @param dictionaryId the id of the dictionary
     * @return the dictionary
     */
    fun open(dictionaryId: Uuid): Dictionary {
        val json = fileService.read(getDictionaryFile(dictionaryId))
        return jsonUtil.deserialize(json)
    }

    /**
     * Import a dictionary from a file on disk. <br></br>
     * Converts the file to the [Dictionary] structure, saving the result.
     *
     * @param dictionaryName the name of the dictionary (needed to find the fitting parser)
     * @param location of the files to import
     * @param language the target language
     * @return the imported dictionary
     */
    fun importFromFile(
        dictionaryName: String,
        location: FileRef,
        language: String
    ): Dictionary {
        Logger.i("importing dictionary '$dictionaryName' in '$language' from '$location'")
        val imported: Dictionary = importerFactory
            .forName(dictionaryName)
            .importFromFile(location, language)
        save(imported)
        return imported
    }

    /**
     * A file ref to the file containing the dictionary as json. <br></br>
     * <bold>DOES NOT CHECK IF THE FILE EXISTS!</bold> <br></br>
     * If no dictionary with the given id has been downloaded, the file won't exist, but this method <bold>WILL</bold> return
     * the theoretical ref. <br></br>
     * The file name will be `dictId.json`.
     *
     * @param dictionaryId the id of the dictionary
     * @return file ref to the metadata file
     */
    fun getDictionaryFile(dictionaryId: Uuid): FileRef {
        return dictionaryDir.resolve("$dictionaryId.json")
    }

    /**
     * Lists the names of all supported dictionaries. <br></br>
     * Supported means that a [dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporter] with the given [dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporter.getDictionaryName] has been registered
     * in [DictionaryImporterFactory].
     *
     * @return a list of all names of supported dictionaries
     */
    fun listSupportedDictionaryNames(): List<String> {
        return DictionaryImporterFactory.registeredDictionaryNames()
    }

    /**
     * Lists the metadata of all downloaded dictionaries.<br></br>
     * The downloads are obtained by looking at [.dictionaryDir] and its children, parsing the metadata fields from the json.<br></br>
     * Therefore, manipulating/adding files in the directory may result in downloaded dictionaries that are not supported
     * or missing, once downloaded, dictionaries.<br></br>
     *
     * @return the list of metadata of downloaded dictionaries in the form of fieldName -> fieldValue
     */
    fun listDownloadedDictionaryMetadata(): List<DictionaryMetadata> {
        return fileService.listChildren(dictionaryDir)
            .map { file ->
                return@map fileService.read(file)
            }
            .map { json ->
                return@map jsonUtil.deserialize<DictionaryMetadata>(json)
            }.toList()
    }

    /**
     * Lookup the word in the given dictionary.
     * Also looks up the substrings for possible words.
     *
     *
     * Example: Egg
     * Lookup: Egg, Eg, E (results also displayed in that order)
     *
     * @param dictionary the dictionary to use
     * @param word the word to lookup
     * @return the list of results, [DictionaryEntry.word]`.length` in descending oder.
     */
    fun lookup(dictionary: Dictionary, word: String): List<DictionaryEntry> {
        val results: MutableList<DictionaryEntry> = ArrayList()
        for (i in word.length downTo 1) {
            val sub = word.take(i)
            val entry = dictionary.entries[sub]
            if (entry != null) results.add(entry)
        }
        return results
    }

    private fun save(toSave: Dictionary) {
        val targetFile = getDictionaryFile(toSave.id)
        Logger.i("saving dictionary '${toSave.name}' to '$targetFile'")
        fileService.write(targetFile, jsonUtil.serialize(toSave))
    }
}
