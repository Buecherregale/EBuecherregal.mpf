package dev.buecherregale.ebook_reader

import app.cash.sqldelight.db.SqlDriver
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

actual fun createSqlDriver(dbName: String, create: Boolean): SqlDriver {
    TODO("Not yet implemented")
}