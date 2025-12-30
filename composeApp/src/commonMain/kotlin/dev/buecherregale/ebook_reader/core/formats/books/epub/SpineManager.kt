package dev.buecherregale.ebook_reader.core.formats.books.epub

import dev.buecherregale.ebook_reader.core.formats.books.NavigationController
import dev.buecherregale.ebook_reader.core.formats.books.epub.xml.Spine
import dev.buecherregale.ebook_reader.core.exception.EPubSyntaxException
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import kotlinx.io.Source

class SpineManager(private val zipFile: ZipFileRef,
                   private val resources: Map<String, EPubResource>,
                   private val spine: Spine,
) : NavigationController {

    private var currentIndex = -1

    override fun next(): EPubNavigationElement? {
        if (currentIndex == spine.itemRefs.size - 1) return null
        return get(++currentIndex)
    }

    override fun prev(): EPubNavigationElement? {
        if (currentIndex <= 0) return null
        return get(--currentIndex)
    }

    override fun get(progress: Double): EPubNavigationElement {
        require(!(progress < 0)) { "progress needs to be [0;1]" }
        currentIndex = if (progress >= 1) spine.itemRefs.size - 1
        else (progress * spine.itemRefs.size).toInt()
        return get(currentIndex) ?: throw IllegalArgumentException("could not get element for progress: $progress")
    }

    override fun progress(): Double {
        return if (currentIndex < 0) 0.0 else (currentIndex.toDouble()) / spine.itemRefs.size
    }

    fun openResource(href: String): Source {
        val entry = zipFile.getEntry(href) ?: throw EPubSyntaxException("resource $href not present")
        return entry.open()
    }

    private fun get(index: Int): EPubNavigationElement? {
        if (index < 0 || index >= spine.itemRefs.size) return null
        val href = spine.itemRefs[index].idref
        return EPubNavigationElement(href, resources)
    }
}
