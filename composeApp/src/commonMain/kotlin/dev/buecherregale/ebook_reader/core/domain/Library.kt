package dev.buecherregale.ebook_reader.core.domain

import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
class Library {
    val name: String
    var image: FileRef? = null

    val bookIds: MutableList<Uuid> = ArrayList()

    constructor(name: String) {
        this.name = name
    }

    constructor(name: String, image: FileRef?) {
        this.name = name
        this.image = image
    }
}
