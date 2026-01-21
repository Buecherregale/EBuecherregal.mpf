package dev.buecherregale.ebook_reader.core.dom.epub

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.dom.DocumentFormat
import dev.buecherregale.ebook_reader.core.dom.FormatDetector
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import kotlinx.io.readString

class EPubFormatDetector : FormatDetector {

    /**
     * This checks if the given file is an epub by opening the zip and checking for the
     * [EPubConstants.MIMETYPE] file and if it contains the correct type ([EPubConstants.CONTENT_TYPE]).
     *
     * DOES NOT check if the mimetype file is the first file in the zip, which is a standardized requirement.
     */
    override suspend fun detect(
        file: FileRef,
        fileService: FileService
    ): DocumentFormat? {
        val zip: ZipFileRef
        try {
            zip = fileService.readZip(file)
        } catch (e: Exception) {
            Logger.d { "could not read zip file: $e" }
            return null
        }
        val mimetype = (zip.getEntry(EPubConstants.MIMETYPE) ?: return null)
            .open()
            .readString()
        return if (mimetype == EPubConstants.CONTENT_TYPE) {
            DocumentFormat.EPUB
        } else {
            null
        }
    }
}