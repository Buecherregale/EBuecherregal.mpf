package dev.buecherregale.ebook_reader.core.service.filesystem

import kotlinx.io.Source

/**
 * A generic interface with different methods for handling files. <br></br>
 * E.g. desktop filesystems or android content management. <br></br>
 * Works in tandem with the right [FileRef] implementation.
 */
interface FileService {
    /**
     * Reads a files whole content as (UTF-8) String.
     *
     * @param file the reference
     * @return the data
     */
    fun read(file: FileRef): String

    /**
     * Reads a files' whole content to a byte array.
     *
     * @param file the reference
     * @return the whole file data
     */
    fun readBytes(file: FileRef): ByteArray

    /**
     * Reads a files whole content as (UTF-8) String.
     *
     * @param directory   the app directory type to write in
     * @param relativeRef the ref to a file in this directory
     * @return the data
     */
    fun read(directory: AppDirectory, relativeRef: FileRef): String

    /**
     * Opens an input stream to the file denoted by the ref.
     *
     * @param file the ref
     * @return an open input stream
     */
    fun open(file: FileRef): Source

    /**
     * Opens the file as a zip.
     *
     * @param file the ref
     * @return the file interpreted as zip
     */
    fun readZip(file: FileRef): ZipFileRef

    /**
     * Writes the content as UTF-8 to the file, creating it if necessary.
     *
     * @param file the ref
     * @param content the content to write
     */
    fun write(file: FileRef, content: String)

    /**
     * Writes the content bytes to the file, creating it if necessary.
     *
     * @param file the ref
     * @param content the content
     */
    fun write(file: FileRef, content: ByteArray)

    /**
     * Copies the values read from the input stream to the file, creating/overwriting it.
     *
     * @param input the input stream
     * @param target the target file reference
     */
    fun copy(input: Source, target: FileRef)

    /**
     * Checks if the given file exists.
     *
     * @param file the target file to check
     * @return if it exists
     */
    fun exists(file: FileRef): Boolean

    /**
     * Obtains metadata from a file.
     *
     * @param file the ref
     * @return the metadata
     */
    fun getMetadata(file: FileRef): FileMetadata

    /**
     * Gets the actual reference to the directory type. <br></br>
     * An example would be to refer the directory types to the xdg like [AppDirectory.CONFIG] to `XDG_CONFIG_HOME`.
     *
     * @param directory the directory type
     * @return the ref
     */
    fun getAppDirectory(directory: AppDirectory): FileRef

    /**
     * Lists the children of a ref, the files inside a folder.
     *
     * @param fileRef the ref
     * @return empty if none exist (or fileRef is a file)
     */
    fun listChildren(fileRef: FileRef): List<FileRef>

    /**
     * Deserialize a reference from a string, creating the instance.
     *
     * @param s a string
     * @return the reference instance
     */
    fun deserializeRef(s: String): FileRef

    /**
     * Serialize a reference to a string.
     *
     * @param fileRef the reference
     * @return the serialization
     * @implNote default refers to [FileRef.path]
     */
    fun serializeRef(fileRef: FileRef): String {
        return fileRef.path
    }

    /**
     * Removes gz compression on the given byte array.
     *
     * @param bytes of a gz-compressed file
     * @return the decompressed bytes
     */
    fun ungzip(bytes: ByteArray): ByteArray
}