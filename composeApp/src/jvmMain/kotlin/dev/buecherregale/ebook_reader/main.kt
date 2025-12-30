package dev.buecherregale.ebook_reader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.buecherregale.ebook_reader.core.formats.books.BookParserFactory
import dev.buecherregale.ebook_reader.core.formats.books.epub.EPubParser
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.formats.dictionaries.jmdict.JMDictImporter
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.core.service.DictionaryService
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.LibraryService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import dev.buecherregale.ebook_reader.filesystem.DesktopFileRef
import dev.buecherregale.ebook_reader.filesystem.DesktopFileService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.uuid.ExperimentalUuidApi

@OptIn(DelicateCoroutinesApi::class, ExperimentalUuidApi::class)
fun main() = application {

    val fileService: FileService = DesktopFileService("ebook-reader")
    val jsonUtil = JsonUtil(fileService)
    DictionaryImporterFactory.register("JmDict", ::JMDictImporter)
    val importerFactory = DictionaryImporterFactory(fileService)
    val dictService = DictionaryService(fileService, jsonUtil, importerFactory)

    GlobalScope.launch {
        val d = dictService.download("JmDict", "eng")
        println(d.name)
        println(d.id)
    }

    val libraryService = LibraryService(fileService, jsonUtil)
    libraryService.createLibrary("test library", null)

    BookParserFactory.register({ file -> EPubParser.isEPub(fileService, file) }, ::EPubParser)
    val bookParserFactory = BookParserFactory(fileService)
    val bookService = BookService(fileService, jsonUtil, bookParserFactory)
    bookService.importBook(DesktopFileRef(Path.of("/home/david/Development/IdeaProjects/EBook-Reader/testbook.epub")))

    Window(
        onCloseRequest = ::exitApplication,
        title = "ebook_reader",
    ) {
        App()
    }
}