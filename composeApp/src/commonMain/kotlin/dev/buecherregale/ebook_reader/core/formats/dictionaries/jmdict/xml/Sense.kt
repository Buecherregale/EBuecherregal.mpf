package dev.buecherregale.ebook_reader.core.formats.dictionaries.jmdict.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("sense", "", "")
data class Sense(
    @XmlElement(true)
    @XmlSerialName("pos", "", "")
    val partsOfSpeech: List<String> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("gloss", "", "")
    val glosses: List<Gloss> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("xref")
    val refs: List<String> = emptyList(),
)
