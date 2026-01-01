package dev.buecherregale.ebook_reader.core.service

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.domain.Library
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.JsonUtil
import kotlinx.io.readByteArray
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Service-API to handle [Library] instances. As the domain classes are DTOs, use service classes to manipulate them.
 */
@OptIn(ExperimentalUuidApi::class)
class LibraryService(
    private val fileService: FileService,
    private val jsonUtil: JsonUtil) {
    private val libDir: FileRef = fileService.getAppDirectory(AppDirectory.STATE).resolve("libraries")

    /**
     * Adds a book to the library by adding the book id. <br></br>
     * Will also save and <bold>WRITE</bold> the library to disk. <br></br>
     * Use this instead of manipulating the [Library.bookIds] directly.
     *
     * @param library the library
     * @param bookId the book to add
     */
    fun addBook(library: Library, bookId: Uuid) {
        library.bookIds.add(bookId)
        saveLibrary(library)
    }

    /**
     * Create a library based on the name and saves it to disk.
     *
     * @param name the desired name
     * @param image nullable, cover image for the library
     * @return the created library
     */
    fun createLibrary(name: String, image: FileRef?): Library {
        Logger.i("creating library '$name'")
        val l: Library
        if (image != null) {
            val imageTarget: FileRef = libDir.resolve("images").resolve(name)
            fileService.copy(fileService.open(image), imageTarget)
            l = Library(name, imageTarget)
        } else l = Library(name)
        saveLibrary(l)
        return l
    }

    /**
     * Create a library based on the name and saves it to disk.
     *
     * @param name the desired name
     * @param imageBytes the bytes for the cover image
     * @return the created library
     */
    fun createLibrary(name: String, imageBytes: ByteArray?): Library {
        Logger.i("creating library '$name'")
        val l: Library
        if (imageBytes != null) {
            val imageTarget: FileRef = libDir.resolve("images").resolve(name)
            fileService.write(imageTarget, imageBytes)
            l = Library(name, imageTarget)
        } else l = Library(name)
        saveLibrary(l)
        return l
    }

    /**
     * Save the library as a json file to the [.libDir] dir.
     * The resulting file will have the name: `libraryName.json`.
     *
     * @param library the library to save
     */
    fun saveLibrary(library: Library) {
        Logger.d("saving library '${library.name}'")
        fileService.write(libDir.resolve(library.name + ".json"), jsonUtil.serialize(library))
    }

    /**
     * Loads a library from the json file by its name.
     *
     * @param name the name of the library
     * @return the deserialized library instance.
     */
    fun loadLibrary(name: String): Library {
        val content: String = fileService.read(libDir.resolve("$name.json"))
        return jsonUtil.deserialize(content)
    }

    /**
     * Loads all libraries in the [.libDir].
     *
     * @return the list of libraries (mutable)
     */
    fun loadLibraries(): List<Library> {
        Logger.d("loading libraries from '$libDir'")

        val libraries: MutableList<Library> = ArrayList()

        for (f in fileService.listChildren(libDir)) {
            if (fileService.getMetadata(f).isDirectory) continue
            val content: String = fileService.read(f)
            val l: Library = jsonUtil.deserialize(content)
            libraries.add(l)
        }
        Logger.i("loaded ${libraries.size} libraries")
        return libraries
    }

    /**
     * Obtains the bytes of the library image.
     * This opens the file and reads the bytes from the source
     *
     * @param library the library
     * @return the bytes of the image file or null if image is not set
     */
    suspend fun imageBytes(library: Library): ByteArray? {
        return library.image?.let { fileService.open(it).readByteArray() }
    }
}
