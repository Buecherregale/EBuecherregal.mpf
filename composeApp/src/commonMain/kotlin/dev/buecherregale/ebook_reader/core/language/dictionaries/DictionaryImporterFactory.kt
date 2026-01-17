package dev.buecherregale.ebook_reader.core.language.dictionaries

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.exception.DictionaryImportException
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService

/**
 * Creates dictionary importers by the name of the dictionary. <br></br>
 * Requires that implementations are registered via [.register],<br></br>
 * providing a [ImporterCreator], to instantiate the implementation, injecting dependencies. <br></br>
 */
class DictionaryImporterFactory(private val fileService: FileService) {
    /**
     * A functional interface to create an implementation of an importer.
     * This is called by the factory to create upon calling [.forName].
     * If no further dependencies are required, a simple reference to the constructor will suffice.
     */
    fun interface ImporterCreator {
        /**
         * Creates an instance of the implementation.
         *
         * @param fileService the factories' file service
         * @return an importer instance
         */
        fun create(fileService: FileService): DictionaryImporter
    }

    /**
     * Creates an importer instance based on the dictionary name. <br></br>
     * throws an exception if no fitting implementation was registered via [.register].
     *
     * @param dictName the dictionary name
     * @return the dictionary importer
     */
    fun forName(dictName: String): DictionaryImporter {
        val creator = creatorByDictionaryName[dictName]
            ?: throw DictionaryImportException("no importer implementation for name: $dictName")
        return creator.create(fileService)
    }

    companion object {
        private val creatorByDictionaryName: MutableMap<String, ImporterCreator> = HashMap()

        /**
         * Registers an implementation based on the supported dictionary name, supplying a way for the factory to create an instance.
         *
         * @param dictionaryName the name of the supported dictionary for which to use this implementation
         * @param creator how to instantiate the implementation
         */
        fun register(dictionaryName: String, creator: ImporterCreator) {
            if (creatorByDictionaryName.containsKey(dictionaryName))
                Logger.w("overwriting importer implementation for dictionary: $dictionaryName")
            creatorByDictionaryName[dictionaryName] = creator
            Logger.i("registered importer for '$dictionaryName'")
        }

        /**
         * Returns all dictionary names which have been registered via [.register].
         *
         * @return the list of names, might be empty
         */
        fun registeredDictionaryNames(): List<String> {
            return creatorByDictionaryName.keys
                .toList()
        }
    }
}
