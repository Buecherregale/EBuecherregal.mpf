package dev.buecherregale.ebook_reader.filesystem

import dev.buecherregale.ebook_reader.core.service.filesystem.ZipEntryRef
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import java.io.IOException
import java.util.zip.ZipFile

data class DesktopZipFileRef(val zipFile: ZipFile) : ZipFileRef {
    override fun getEntry(pathInZip: String): ZipEntryRef {
        return DesktopZipEntryRef(zipFile, zipFile.getEntry(pathInZip))
    }

    @Throws(IOException::class)
    override fun close() {
        zipFile.close()
    }
}
