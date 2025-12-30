package dev.buecherregale.ebook_reader.core.config

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Class for centralized state management.
 */
@OptIn(ExperimentalUuidApi::class)
class ApplicationSettings {
    internal var activeDictionaryId: Uuid? = null
}
