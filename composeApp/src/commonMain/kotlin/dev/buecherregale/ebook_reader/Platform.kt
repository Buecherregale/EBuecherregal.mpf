package dev.buecherregale.ebook_reader

import app.cash.sqldelight.db.SqlDriver
import org.koin.core.module.Module

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun platformModule(): Module

expect suspend fun pickBook(): PickedFile?
expect suspend fun pickImage(): PickedImage?

expect fun createSqlDriver(): SqlDriver


data class PickedFile(
    val path: String
)

data class PickedImage(
    val name: String?,
    val bytes: ByteArray,
    val mimeType: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PickedImage

        if (name != other.name) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        return result
    }
}
