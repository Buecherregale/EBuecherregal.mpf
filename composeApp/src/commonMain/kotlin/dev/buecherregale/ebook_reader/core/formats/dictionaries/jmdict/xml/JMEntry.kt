package dev.buecherregale.ebook_reader.core.formats.dictionaries.jmdict.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("entry", "", "")
data class JMEntry(
    @XmlElement(true)
    @XmlSerialName("ent_seq", "", "")
    val sequence: Long,

    @XmlElement(true)
    @XmlSerialName("k_ele", "", "")
    val kanjiElements: List<KanjiElement> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("r_ele", "", "")
    val readingElements: List<ReadingElement> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("sense", "", "")
    val senses: List<Sense> = emptyList()
)
