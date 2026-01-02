package dev.buecherregale.ebook_reader.core.service

import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.core.domain.Library
import dev.buecherregale.ebook_reader.core.repository.LibraryRepository
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Service-API to handle [Library] instances. As the domain classes are DTOs, use service classes to manipulate them.
 */
@OptIn(ExperimentalUuidApi::class)
class LibraryService(
    private val fileService: FileService,
    private val repository: LibraryRepository) {
    private val libDir: FileRef = fileService.getAppDirectory(AppDirectory.STATE).resolve("libraries")

    /**
     * Adds a book to the library by adding the book id. <br></br>
     * Will also save and <bold>WRITE</bold> the library to disk. <br></br>
     * Use this instead of manipulating the [Library.bookIds] directly.
     *
     * @param library the library
     * @param bookId the book to add
     */
    suspend fun addBook(library: Library, bookId: Uuid) {
        library.bookIds.add(bookId)
        repository.save(library.name, library)
    }

    /**
     * Create a library based on the name and saves it to disk.
     *
     * @param name the desired name
     * @param image nullable, cover image for the library
     * @return the created library
     */
    suspend fun createLibrary(name: String, image: FileRef?): Library {
        Logger.i("creating library '$name'")
        val l: Library
        if (image != null) {
            val bytes = fileService.readBytes(image)
            val imageTarget: FileRef = libDir.resolve("images").resolve(name)
            repository.saveImage(name, bytes)
            l = Library(name, imageTarget)
        } else l = Library(name)
        repository.save(l.name, l)
        return l
    }

    /**
     * Create a library based on the name and saves it to disk.
     *
     * @param name the desired name
     * @param imageBytes the bytes for the cover image
     * @return the created library
     */
    suspend fun createLibrary(name: String, imageBytes: ByteArray?): Library {
        Logger.i("creating library '$name'")
        val l: Library
        if (imageBytes != null) {
            val imageTarget: FileRef = libDir.resolve("images").resolve(name)
            repository.saveImage(name, imageBytes)
            l = Library(name, imageTarget)
        } else l = Library(name)
        repository.save(l.name, l)
        return l
    }

    /**
     * Loads a library from the json file by its name.
     *
     * @param name the name of the library
     * @return the deserialized library instance.
     */
    suspend fun loadLibrary(name: String): Library {
        return repository.load(name) ?: throw IllegalArgumentException("library $name does not exist")
    }

    /**
     * Loads all libraries in the [.libDir].
     *
     * @return the list of libraries (mutable)
     */
    suspend fun loadLibraries(): List<Library> {
        Logger.d("loading libraries from '$libDir'")

        val libraries = repository.loadAll()

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
        return library.image?.let { repository.readImage(library.name) }
    }
}
