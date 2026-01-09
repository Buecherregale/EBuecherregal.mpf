package dev.buecherregale.ebook_reader.core.dom

import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService

enum class DocumentFormat {
    EPUB,
    PDF
}

interface FormatDetector {
    suspend fun detect(
        file: FileRef,
        fileService: FileService
    ): DocumentFormat?
}


interface DocumentParser {

    val format: DocumentFormat

    suspend fun parse(
        file: FileRef,
        fileService: FileService
    ) : DomDocument
}