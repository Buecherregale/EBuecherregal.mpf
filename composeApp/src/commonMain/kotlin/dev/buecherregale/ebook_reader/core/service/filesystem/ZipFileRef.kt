package dev.buecherregale.ebook_reader.core.service.filesystem

/**
 * A generic reference to a file interpreted as a zip file with its entries. <br></br>
 * Example implementation could just refer to [java.util.zip.ZipFile].
 */
interface ZipFileRef : AutoCloseable {
    /**
     * Gets a reference to the entry by its name/path in the zip.
     *
     * @param pathInZip filename or path like subfolder/filename
     * @return the reference to the zip entry or `null` if it does not exist
     */
    fun getEntry(pathInZip: String): ZipEntryRef?
}
