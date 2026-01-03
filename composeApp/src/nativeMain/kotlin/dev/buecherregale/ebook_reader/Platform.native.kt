package dev.buecherregale.ebook_reader

import app.cash.sqldelight.db.SqlDriver
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import org.koin.core.module.Module

actual fun platformModule(): Module {
    TODO("Not yet implemented")
}

actual suspend fun pickImage(): PickedImage? {
    TODO("Not yet implemented")
}

actual suspend fun pickBook(): PickedFile? {
    TODO("Not yet implemented")
}

actual fun createSqlDriver(fileService: FileService, appName: String): SqlDriver {
    TODO("Not yet implemented")
}