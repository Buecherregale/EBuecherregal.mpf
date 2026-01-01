package dev.buecherregale.ebook_reader.core.service

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.domain.DictionaryEntry
import dev.buecherregale.ebook_reader.core.domain.DictionaryMetadata
import dev.buecherregale.ebook_reader.core.repository.DictionaryRepository
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
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
class DictionaryService(
    private val importerFactory: DictionaryImporterFactory,
    private val repository: DictionaryRepository,
) {
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
        Logger.i("saving dictionary '${downloaded.name}'")
        repository.save(downloaded.id, downloaded)
        return downloaded
    }

    /**
     * Opens an existing dictionary file, deserializing its content. <br></br>
     * Expects the dictionary at the [.getDictionaryFile] file.
     *
     * @param dictionaryId the id of the dictionary
     * @return the dictionary
     */
    suspend fun open(dictionaryId: Uuid): Dictionary {
        return repository.load(dictionaryId)
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
    suspend fun importFromFile(
        dictionaryName: String,
        location: FileRef,
        language: String
    ): Dictionary {
        Logger.i("importing dictionary '$dictionaryName' in '$language' from '$location'")
        val imported: Dictionary = importerFactory
            .forName(dictionaryName)
            .importFromFile(location, language)
        Logger.i("saving dictionary '${imported.name}'")
        repository.save(imported.id, imported)
        return imported
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
    suspend fun listDownloadedDictionaryMetadata(): List<DictionaryMetadata> {
        return repository.loadMetadata()
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
}
