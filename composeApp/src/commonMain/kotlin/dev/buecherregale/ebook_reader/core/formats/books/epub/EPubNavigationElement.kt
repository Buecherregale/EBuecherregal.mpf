package dev.buecherregale.ebook_reader.core.formats.books.epub

/**
 * The navigation element in an Epub denoted in the spine of the epub.
 * Consists of the Href to the content file to be displayed and a map of the Href to available resources (such has css) by id.
 * <br></br>
 * TODO: check if this class is needed, as SpineManager provides access to resources. Alternatively create an interface (when next type gets supported)
 */
data class EPubNavigationElement(val contentHref: String,
                            val resources: Map<String, EPubResource>)