package dev.buecherregale.ebook_reader.core.domain

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.language.LocaleSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class DictionaryMetadata(
    val id: Uuid,
    val name: String,
    @Serializable(with = LocaleSerializer::class)
    val language: Locale,
)
