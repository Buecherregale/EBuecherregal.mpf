package dev.buecherregale.ebook_reader.filesystem

import dev.buecherregale.ebook_reader.core.service.filesystem.ZipEntryRef
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

data class AndroidZipEntryRef(val parent: ZipFile, val entry: ZipEntry) : ZipEntryRef {

    override suspend fun open(): Source {
        return parent.getInputStream(entry).asSource().buffered()
    }
}
