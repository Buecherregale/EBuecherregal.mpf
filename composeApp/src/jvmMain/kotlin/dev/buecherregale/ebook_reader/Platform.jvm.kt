package dev.buecherregale.ebook_reader

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.intl.Locale
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ibm.icu.text.BreakIterator
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.filesystem.DesktopFileService
import dev.buecherregale.ebook_reader.ui.components.SelectedText
import dev.buecherregale.ebook_reader.ui.pickFile
import dev.buecherregale.sql.Buecherregal
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun platformModule(): Module {
    return module {
        single { DesktopFileService("ebook-reader") } binds arrayOf(FileService::class)
    }
}
fun FileRef.toPath(): Path {
    return Paths.get(path)
}

actual suspend fun pickImage(): PickedImage? = pickFile(
    "Images",
    listOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
) { file: File? ->
    if (file == null) return@pickFile null
    PickedImage(
        name = file.name,
        bytes = file.readBytes(),
        mimeType = file.toURI().toURL().openConnection().contentType
    )
}

actual suspend fun pickBook(): PickedFile? = pickFile(
    "Books",
    listOf("pdf", "epub", "txt", "md")
) { file: File? ->
    if (file == null) return@pickFile null
    PickedFile(file.path)
}

actual fun createSqlDriver(fileService: FileService, appName: String): SqlDriver {
    val dbFile = fileService.getAppDirectory(AppDirectory.STATE).resolve("$appName.db")
        .toPath().toFile()

    dbFile.parentFile?.mkdirs()

    return JdbcSqliteDriver(
        url = "jdbc:sqlite:${dbFile.absolutePath}",
        schema = Buecherregal.Schema
    )
}

actual fun findWordInSelection(selection: SelectedText, locale: Locale): TextRange? {
    val text = selection.text
    val index = selection.index

    if (index !in text.indices) return null
    val iterator = BreakIterator.getWordInstance(java.util.Locale.forLanguageTag(locale.toLanguageTag()))
    iterator.setText(text)

    val start = iterator.preceding(index + 1)
    val end = iterator.following(index)

    if (start == BreakIterator.DONE || end == BreakIterator.DONE) return null

    return TextRange(start, end)
}