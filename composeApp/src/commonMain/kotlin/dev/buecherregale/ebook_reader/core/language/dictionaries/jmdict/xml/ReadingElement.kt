package dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("r_ele", "", "")
data class ReadingElement(
    @XmlElement(true)
    @XmlSerialName("reb", "", "")
    val reb: String,

    @XmlElement(true)
    @XmlSerialName("re_pri", "", "")
    val priorities: List<String> = emptyList()
)
