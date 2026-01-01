package dev.buecherregale.ebook_reader

import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.formats.books.BookParserFactory
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.repository.JsonLibraryRepository
import dev.buecherregale.ebook_reader.core.repository.LibraryRepository
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.core.service.DictionaryService
import dev.buecherregale.ebook_reader.core.service.LibraryService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module

@OptIn(KoinExperimentalAPI::class)
val commonModule: Module = module {
    singleOf(::JsonUtil)

    singleOf(::JsonLibraryRepository) binds arrayOf(LibraryRepository::class)

    singleOf(::SettingsManager)
    singleOf(::BookService)
    singleOf(::DictionaryService)
    singleOf(::LibraryService)

    singleOf(::BookParserFactory)
    singleOf(::DictionaryImporterFactory)
}