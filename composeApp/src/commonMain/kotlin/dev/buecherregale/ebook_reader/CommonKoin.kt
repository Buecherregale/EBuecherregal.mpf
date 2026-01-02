package dev.buecherregale.ebook_reader

import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.formats.books.BookParserFactory
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.repository.BookCoverRepository
import dev.buecherregale.ebook_reader.core.repository.BookFileRepository
import dev.buecherregale.ebook_reader.core.repository.BookRepository
import dev.buecherregale.ebook_reader.core.repository.FileRepository
import dev.buecherregale.ebook_reader.core.repository.JsonBookRepository
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

    singleOf(::JsonUtil)

    singleOf(::LibrarySqlRepository) binds arrayOf(LibraryRepository::class)
    singleOf(::JsonBookRepository) binds arrayOf(BookRepository::class)
    single { FileRepository<Uuid>(
        keyToFilename = { name -> "$name.cover" },
        get<FileService>().getAppDirectory(AppDirectory.STATE).resolve("libraries"),
        get()
    ) } binds arrayOf(LibraryImageRepository::class)
    single { FileRepository<Uuid>(
        keyToFilename = { name -> "$name.cover" },
        get<FileService>().getAppDirectory(AppDirectory.STATE).resolve("books"),
        get()
    ) } binds arrayOf(BookCoverRepository::class)
    single { FileRepository<Uuid>(
        keyToFilename = { name -> "$name.book" },
        get<FileService>().getAppDirectory(AppDirectory.STATE).resolve("books"),
        get()
    ) } binds arrayOf(BookFileRepository::class)

    singleOf(::SettingsManager)
    singleOf(::BookService)
    singleOf(::DictionaryService)
    singleOf(::LibraryService)

    singleOf(::BookParserFactory)
    singleOf(::DictionaryImporterFactory)
}