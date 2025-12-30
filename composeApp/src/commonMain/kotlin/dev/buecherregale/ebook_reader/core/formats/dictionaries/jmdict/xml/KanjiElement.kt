package dev.buecherregale.ebook_reader.core.formats.dictionaries.jmdict.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("k_ele", "", "")
data class KanjiElement(
    @XmlElement(true)
    @XmlSerialName("keb", "", "")
    val keb: String,

    @XmlElement(true)
    @XmlSerialName("ke_pri", "", "")
    val priorities: List<String> = emptyList()
)
