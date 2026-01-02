package dev.buecherregale.ebook_reader

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.buecherregale.ebook_reader.core.domain.BookType
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.filesystem.DesktopFileService
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
    BookType.entries.map { it.extension }.toList()
) { file: File? ->
    if (file == null) return@pickFile null
    PickedFile(file.path)
}

actual fun createSqlDriver(dbName: String, create: Boolean): SqlDriver {
    return JdbcSqliteDriver("jdbc:sqlite:$dbName.db").also {
        if (create) Buecherregal.Schema.create(it)
    }
}
