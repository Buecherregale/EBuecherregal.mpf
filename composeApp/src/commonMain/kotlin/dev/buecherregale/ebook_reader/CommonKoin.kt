package dev.buecherregale.ebook_reader

import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.formats.books.BookParserFactory
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.repository.BookCoverRepository
import dev.buecherregale.ebook_reader.core.repository.BookFileRepository
import dev.buecherregale.ebook_reader.core.repository.BookRepository
import dev.buecherregale.ebook_reader.core.repository.BookSqlRepository
import dev.buecherregale.ebook_reader.core.repository.DictionaryEntryRepository
import dev.buecherregale.ebook_reader.core.repository.DictionaryRepository
import dev.buecherregale.ebook_reader.core.repository.DictionarySqlRepository
import dev.buecherregale.ebook_reader.core.repository.FileRepository
import dev.buecherregale.ebook_reader.core.repository.LibraryImageRepository
import dev.buecherregale.ebook_reader.core.repository.LibraryRepository
import dev.buecherregale.ebook_reader.core.repository.LibrarySqlRepository
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.core.service.DictionaryService
import dev.buecherregale.ebook_reader.core.service.LibraryService
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import dev.buecherregale.sql.Buecherregal
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(KoinExperimentalAPI::class, ExperimentalUuidApi::class)
val commonModule: Module = module {
    single { Buecherregal(createSqlDriver(
        dbName = "ebook-reader",
        create = false
    )) }
    single { get<Buecherregal>().librariesQueries }
    single { get<Buecherregal>().booksQueries }
    single { get<Buecherregal>().dictionariesQueries }

    singleOf(::JsonUtil)

    singleOf(::LibrarySqlRepository) binds arrayOf(LibraryRepository::class)
    single { FileRepository<Uuid>(
        keyToFilename = { name -> "$name.cover" },
        get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("libraries"),
        get()
    ) } binds arrayOf(LibraryImageRepository::class)
    singleOf(::BookSqlRepository) binds arrayOf(BookRepository::class)
    single { FileRepository<Uuid>(
        keyToFilename = { name -> "$name.cover" },
        get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("books"),
        get()
    ) } binds arrayOf(BookCoverRepository::class)
    single { FileRepository<Uuid>(
        keyToFilename = { name -> "$name.book" },
        get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("books"),
        get()
    ) } binds arrayOf(BookFileRepository::class)
    singleOf(::DictionarySqlRepository) binds arrayOf(DictionaryRepository::class)
    single { FileRepository<Uuid>(
        keyToFilename = { id -> "$id.json" },
        get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("dictionaries"),
        get()
        ) } binds arrayOf(DictionaryEntryRepository::class)

    singleOf(::SettingsManager)
    singleOf(::BookService)
    singleOf(::DictionaryService)
    singleOf(::LibraryService)

    singleOf(::BookParserFactory)
    singleOf(::DictionaryImporterFactory)
}