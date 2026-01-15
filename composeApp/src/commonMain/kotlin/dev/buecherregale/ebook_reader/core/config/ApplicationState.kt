package dev.buecherregale.ebook_reader.core.config

import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.domain.Library

class ApplicationState {
    var activeDictionaries: MutableMap<String, Dictionary> = mutableMapOf()
    var openBook: Book? = null
    var openLibrary: Library? = null
}
