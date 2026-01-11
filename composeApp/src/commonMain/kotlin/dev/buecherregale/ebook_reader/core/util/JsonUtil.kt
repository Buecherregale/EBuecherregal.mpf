package dev.buecherregale.ebook_reader.core.util

import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import kotlinx.serialization.json.Json

/**
 * Utility class for simple generic Json handling with jackson.
 * Not static, as it relies on [FileService] to serialize and deserialize [dev.buecherregale.ebook_reader.core.service.filesystem.FileRef].
 */
class JsonUtil {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Serializes the given object to an indented string.<br></br>
     * Uses the mapper configured in [.JsonUtil].
     *
     * @param o the object to serialize
     * @return the json string
     */
    inline fun <reified T>serialize(o: T): String {
        return json.encodeToString(o)
    }

    /**
     * Deserialize the given string to an instance of the class provided.<br></br>
     * Uses the mapper configured in [.JsonUtil].
     *
     * @param jsonString the json string
     * @return instance of clazz
     * @param <T> the type to deserialize as
    </T> */
    inline fun <reified T> deserialize(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }
}
