package dev.buecherregale.ebook_reader.core.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class Book(
    val id: Uuid,
    val progress: Double,
    val metadata: BookMetadata
)