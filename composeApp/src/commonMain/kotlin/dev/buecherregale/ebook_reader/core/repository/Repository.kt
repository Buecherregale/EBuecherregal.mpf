package dev.buecherregale.ebook_reader.core.repository

/**
 * A generic repository aimed at persisting data.
 *
 * @param Key the type of unique key to access/save data by
 * @param T the type to persist
 */
interface Repository<Key, T> {
    /**
     * Loads all data from the repository.
     */
    suspend fun loadAll(): List<T>

    /**
     * Loads data with the given key.
     */
    suspend fun load(key: Key): T?

    /**
     * Saves data at the given key.
     */
    suspend fun save(key: Key, value: T)

    /**
     * Deletes data with the given key.
     */
    suspend fun delete(key: Key)
}