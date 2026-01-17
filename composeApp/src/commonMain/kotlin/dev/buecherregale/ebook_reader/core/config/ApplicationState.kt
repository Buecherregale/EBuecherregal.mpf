package dev.buecherregale.ebook_reader.core.config

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.domain.Dictionary

class ApplicationState {
    /**
     * Active dictionaries by the language they translate **from**.
     *
     * @see Dictionary.originalLanguage
     */
    var activeDictionaries: MutableMap<Locale, Dictionary> = mutableMapOf()
}
