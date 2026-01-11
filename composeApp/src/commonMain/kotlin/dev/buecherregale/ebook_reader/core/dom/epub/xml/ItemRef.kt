package dev.buecherregale.ebook_reader.core.dom.epub.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("itemref")
data class ItemRef(
    val idref: String)