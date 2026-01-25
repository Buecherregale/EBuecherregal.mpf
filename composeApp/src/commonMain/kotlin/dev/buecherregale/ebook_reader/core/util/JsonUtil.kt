package dev.buecherregale.ebook_reader.core.util

import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

/**
 * Utility class for simple generic JSON handling with jackson.
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
     * @return the JSON string
     */
    inline fun <reified T> serialize(o: T): String {
        return json.encodeToString(o)
    }

    /**
     * Serializes the given object to the given sink.<br></br>
     * Uses the mapper configured in [.JsonUtil].
     *
     * @param o the object to serialize
     * @param sink the sink to write to
     */
    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> serialize(o: T, sink: Sink) {
        json.encodeToSink(o, sink)
    }

    /**
     * Deserialize the given string to an instance of the class provided.<br></br>
     * Uses the mapper configured in [.JsonUtil].
     *
     * @param jsonString the JSON string
     * @return instance of clazz
     * @param <T> the type to deserialize as
    </T> */
    inline fun <reified T> deserialize(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }

    /**
     * Deserialize the given source to an instance of the class provided.<br></br>
     * Uses the mapper configured in [.JsonUtil].
     *
     * @param source the source to read from
     * @return instance of clazz
     * @param <T> the type to deserialize as
    </T> */
    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> deserialize(source: Source): T {
        return json.decodeFromSource(source)
    }
}
