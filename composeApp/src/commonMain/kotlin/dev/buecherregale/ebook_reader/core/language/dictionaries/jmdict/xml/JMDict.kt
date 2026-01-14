package dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("JMdict")
data class JMDict(
    @XmlElement(true)
    @XmlSerialName("entry")
    val entries: List<JMEntry>
)
