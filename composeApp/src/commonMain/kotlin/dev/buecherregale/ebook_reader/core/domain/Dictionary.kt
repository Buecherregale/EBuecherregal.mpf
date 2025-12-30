package dev.buecherregale.ebook_reader.core.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Dictionary(
    val id: Uuid,
    val name: String,
    val language: String,
    val entries: Map<String, DictionaryEntry>
)