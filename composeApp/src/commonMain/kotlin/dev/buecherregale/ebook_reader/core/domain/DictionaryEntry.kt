package dev.buecherregale.ebook_reader.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryEntry(
    val word: String,
    val reading: String,
    val meaning: String,
    val partsOfSpeech: List<String>,
    val common: Boolean
)
