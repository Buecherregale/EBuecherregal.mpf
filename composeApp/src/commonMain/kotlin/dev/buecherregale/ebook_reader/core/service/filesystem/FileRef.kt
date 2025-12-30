package dev.buecherregale.ebook_reader.core.service.filesystem

/**
 * A generic reference to a file to be utilized with different methods of handling files. <br></br>
 * E.g. desktop filesystems or android content management.
 */
interface FileRef {
    /**
     * Writes the FileRef as a string (might be used for serialization).
     *
     * @return the string representation
     */
    override fun toString(): String

    /**
     * Gives the file ref created by "appending" the other ref to this one. <br></br>
     * Similar functionality to [java.nio.file.Path.resolve].
     *
     * @param other the other ref
     * @return the resulting ref
     */
    fun resolve(other: FileRef): FileRef

    /**
     * Gives the file ref created by "appending" the string to this one. <br></br>
     * The string represents a file name, thus this method should resolve to a file
     * by the name in a directory represented by this ref. <br></br>
     * Similar functionality to [java.nio.file.Path.resolve].
     *
     * @param other a file name as string
     * @return the resulting ref
     */
    fun resolve(other: String): FileRef
}
