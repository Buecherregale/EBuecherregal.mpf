package dev.buecherregale.ebook_reader.core.domain

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.language.LocaleSerializer
import kotlinx.serialization.Serializable

@Serializable
data class BookMetadata(
    val title: String,
    val author: String,
    val isbn: String,
    @Serializable(with = LocaleSerializer::class)
    val language: Locale,
)
