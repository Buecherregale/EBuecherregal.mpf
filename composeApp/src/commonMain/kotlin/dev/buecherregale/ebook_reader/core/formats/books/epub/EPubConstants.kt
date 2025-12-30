package dev.buecherregale.ebook_reader.core.formats.books.epub

/**
 * Class containing constants for use in epub xml
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

        /* ********** */ /* TAG NAMES */ /* ********** */
        const val TAG_ROOTFILE: String = "rootfile"

        const val TAG_METADATA: String = "metadata"
        const val TAG_METADATA_META: String = "meta"
        const val TAG_METADATA_DC_PREFIX: String = "dc:"
        const val TAG_MANIFEST: String = "manifest"
        const val TAG_SPINE: String = "spine"

        const val TAG_ITEM: String = "item" // opf manifest item - available resource
        const val TAG_ITEMREF: String = "itemref" // has TAG_IDREF attr, references an item with same id

        /* METADATA */
        const val TAG_TITLE: String = "dc:title"
        const val TAG_AUTHOR: String = "dc:creator"
        const val TAG_LANGUAGE: String = "dc:language"
        const val TAG_BOOK_ID: String = "dc:identifier"

        /* ********** */ /* ATTR NAMES */ /* ********** */
        const val ATTR_OPF_PATH: String = "full-path"
        const val ATTR_ID: String = "id"
        const val ATTR_HREF: String = "href"
        const val ATTR_MEDIA_TYPE: String = "media-type"
        const val ATTR_IDREF: String = "idref"
        const val ATTR_METADATA_NAME: String = "name"
        const val ATTR_METADATA_CONTENT: String = "content"
    }
}
