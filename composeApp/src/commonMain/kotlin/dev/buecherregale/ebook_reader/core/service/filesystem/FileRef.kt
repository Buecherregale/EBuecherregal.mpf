package dev.buecherregale.ebook_reader.core.service.filesystem

import kotlinx.serialization.Serializable

/**
 * A simple value based FileRef implemented as a String.
 *
 * Other platforms should implement extension functions to use this class
 * with their [FileService] implementation.
 */
@Serializable
data class FileRef(val path: String) {
    /**
     * Quick way to merge two file refs by appending the other. <br>
     * Similar functionality to [java.nio.file.Path.resolve]. <br>
     *
     * Merges paths via `/`. Users need to be sure this is supported on their platform.
     *
     * @param other the string to add to the path.
     * @return the new ref
     */
    fun resolve(other: String): FileRef = FileRef("$path/$other")

    /**
     * Quick way to merge two file refs by appending the other. <br>
     * Similar functionality to [java.nio.file.Path.resolve]. <br>
     *
     * Merges paths via `/`. Users need to be sure this is supported on their platform.
     *
     * @param other the string to add to the path.
     * @return the new ref
     */
    fun resolve(other: FileRef): FileRef = FileRef("$path/$other")
}
