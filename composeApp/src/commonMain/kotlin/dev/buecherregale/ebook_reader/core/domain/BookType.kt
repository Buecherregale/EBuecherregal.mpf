package dev.buecherregale.ebook_reader.core.domain

import kotlinx.serialization.Serializable

/**
 * Types of books. This enum should contain at least all supported book types.
 */
@Serializable
enum class BookType(
    val extension: String,
) {
    PDF("pdf"),
    EPUB("epub"),
    TXT("txt");


}
