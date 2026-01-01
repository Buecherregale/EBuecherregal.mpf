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
     * Loads all data from the repository where the predicate returns true.
     *
     * NOTE: default implementation will load ALL data and filter afterwards.
     */
    suspend fun loadWhere(predicate: (T) -> Boolean): List<T> = loadAll().filter(predicate)

    /**
     * Loads data with the given key.
     */
    suspend fun load(key: Key): T

    /**
     * Saves data at the given key.
     */
    suspend fun save(key: Key, value: T)

    /**
     * Deletes data with the given key.
     */
    suspend fun delete(key: Key)
}