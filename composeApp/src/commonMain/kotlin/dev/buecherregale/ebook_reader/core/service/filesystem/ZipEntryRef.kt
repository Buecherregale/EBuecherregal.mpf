package dev.buecherregale.ebook_reader.core.service.filesystem

import kotlinx.io.Source

/**
 * Generic reference to an entry within a zip. <br></br>
 * Example implementation could just refer to [java.util.zip.ZipEntry].
 */
interface ZipEntryRef {
    /**
     * Opens an input stream allowing to read the content of the file.
     *
     * @return an open input stream
     */
    suspend fun open(): Source
}
