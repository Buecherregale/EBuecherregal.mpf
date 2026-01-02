package dev.buecherregale.ebook_reader.ui.navigation

import androidx.navigation3.runtime.NavKey
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.Library
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object LibraryOverview : Screen
    @Serializable
    data class LibraryDetail(val library: Library) : Screen

    @Serializable
    data class Reader(val book: Book) : Screen
    @Serializable
    data object Settings : Screen
}
