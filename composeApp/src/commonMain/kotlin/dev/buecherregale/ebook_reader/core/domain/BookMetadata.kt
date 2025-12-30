package dev.buecherregale.ebook_reader.core.domain

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlOtherAttributes

@Serializable
data class BookMetadata(
    val title: String,
    val author: String,
    val isbn: String,
    val language: String,
)
