package dev.buecherregale.ebook_reader.filesystem

import dev.buecherregale.ebook_reader.core.service.filesystem.ZipEntryRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

data class DesktopZipEntryRef(val parent: ZipFile, val entry: ZipEntry) : ZipEntryRef {

    override suspend fun open(): Source {
        return withContext(Dispatchers.IO) {
            parent.getInputStream(entry)
        }.asSource().buffered()
    }
}
