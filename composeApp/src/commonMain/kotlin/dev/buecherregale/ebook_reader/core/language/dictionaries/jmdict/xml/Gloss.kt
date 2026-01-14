package dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("gloss", "", "")
data class Gloss(
    @XmlValue(true)
    val text: String,

    //@XmlAttr("lang", "http://www.w3.org/XML/1998/namespace")
    val lang: String? = null
)
