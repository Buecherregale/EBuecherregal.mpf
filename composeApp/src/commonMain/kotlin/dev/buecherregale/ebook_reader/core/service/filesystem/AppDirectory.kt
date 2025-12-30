package dev.buecherregale.ebook_reader.core.service.filesystem

/**
 * The directory types this application writes in, inspired by the xdg standard. <br></br>
 * Where these directories live (and if they even are different directories) is up to the [FileService] implementation. <br></br>
 * There is no guarantee this will work if multiple types point at the same directory, but it is likely.
 *
 * @see FileService.getAppDirectory
 */
enum class AppDirectory {
    /**
     * Config directory to save user settings
     */
    CONFIG,

    /**
     * Directory for data like collections/libraries
     */
    DATA,

    /**
     * Directory for files representing the current state
     */
    STATE
}
