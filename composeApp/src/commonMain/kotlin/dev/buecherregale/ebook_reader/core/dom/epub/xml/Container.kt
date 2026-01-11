package dev.buecherregale.ebook_reader.core.dom.epub.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

private const val CONTAINER_NS =
    "urn:oasis:names:tc:opendocument:xmlns:container"

@Serializable
@XmlSerialName("container", CONTAINER_NS)
data class Container(
    val rootfiles: RootFiles
)

@Serializable
@XmlSerialName("rootfiles")
data class RootFiles(
    @XmlElement(true)
    @XmlSerialName("rootfile")
    val rootfile: List<RootFile>
)

@Serializable
@XmlSerialName("rootfile")
data class RootFile(

    @XmlElement(false)
    @XmlSerialName("full-path")
    val fullPath: String,

    @XmlElement(false)
    @XmlSerialName("media-type")
    val mediaType: String
)
