package dev.buecherregale.ebook_reader.core.dom.epub

/**
 * Class containing constants for use in epub XML
 */
interface EPubConstants {
    companion object {
        /* FILE NAMES */
        const val MIMETYPE: String = "mimetype"
        const val CONTAINER_XML: String = "META-INF/container.xml"

        /* CONTENT */
        const val CONTENT_TYPE: String = "application/epub+zip"
        const val PREFERRED_IDENTIFIER_SCHEME = "MOBI-ASIN"
        const val COVER_XML_ID = "cover"

    }
}