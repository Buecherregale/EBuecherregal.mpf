package dev.buecherregale.ebook_reader.core.formats.books.epub.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("itemref")
data class ItemRef(
    val idref: String)