package dev.buecherregale.ebook_reader.core.language.dictionaries

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef

/**
 * Class to import a certain dictionary from the web or from a file into the app.
 */
interface DictionaryImporter {
    /**
     * Imports a dictionary from a file, loading it into memory and adjusting it to the internal app structure ([Dictionary]).
     *
     * @param file     the ref to the file
     * @param language target language
     * @return the loaded dictionary
     */
    suspend fun importFromFile(file: FileRef, language: Locale): Dictionary

    /**
     * Downloads the dictionary.
     *
     * @return the downloaded bytes
     */
    suspend fun download(): ByteArray

    /**
     * Downloads the dictionary, transforming it to the app format.
     *
     * @param language the target language (dictionary is translating to)
     * @return the downloaded and transformed dictionary
     */
    suspend fun download(language: Locale): Dictionary

    val dictionaryName: String
}
