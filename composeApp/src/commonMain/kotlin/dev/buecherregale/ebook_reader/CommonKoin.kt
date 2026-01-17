package dev.buecherregale.ebook_reader

import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.language.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.repository.BookCoverRepository
import dev.buecherregale.ebook_reader.core.repository.BookFileRepository
import dev.buecherregale.ebook_reader.core.repository.BookRepository
import dev.buecherregale.ebook_reader.core.repository.BookSqlRepository
import dev.buecherregale.ebook_reader.core.repository.DictionaryEntryRepository
import dev.buecherregale.ebook_reader.core.repository.DictionaryMetadataRepository
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

@OptIn(KoinExperimentalAPI::class, ExperimentalUuidApi::class)
val commonModule: Module = module {
    single {
        Buecherregal(
            createSqlDriver(
                get<FileService>(),
                appName = "ebook-reader"
            )
        )
    }
    single { get<Buecherregal>().librariesQueries }
    single { get<Buecherregal>().booksQueries }
    single { get<Buecherregal>().dictionariesQueries }

    singleOf(::JsonUtil)

    singleOf(::LibrarySqlRepository) binds arrayOf(LibraryRepository::class)
    single {
        LibraryImageRepository(
            delegate = FileRepository(
                keyToFilename = { name -> "$name.cover" },
                get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("libraries"),
                get()
            )
        )
    }
    singleOf(::BookSqlRepository) binds arrayOf(BookRepository::class)
    single {
        BookCoverRepository(
            delegate = FileRepository(
                keyToFilename = { name -> "$name.cover" },
                get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("books"),
                get()
            )
        )
    }
    single {
        BookFileRepository(
            delegate = FileRepository(
                keyToFilename = { name -> "$name.book" },
                get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("books"),
                get()
            )
        )
    }
    singleOf(::DictionarySqlRepository) binds arrayOf(DictionaryMetadataRepository::class)
    single {
        DictionaryEntryRepository(
            delegate = FileRepository(
                keyToFilename = { id -> "$id.json" },
                get<FileService>().getAppDirectory(AppDirectory.DATA).resolve("dictionaries"),
                get()
            )
        )
    }

    singleOf(::SettingsManager)
    singleOf(::BookService)
    singleOf(::DictionaryService)
    singleOf(::LibraryService)

    singleOf(::DictionaryImporterFactory)
}