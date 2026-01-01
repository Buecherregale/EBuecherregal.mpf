package dev.buecherregale.ebook_reader.ui.navigation

import androidx.navigation3.runtime.NavKey
import dev.buecherregale.ebook_reader.core.domain.Library
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
sealed interface Screen : NavKey {
    @Serializable
    data object LibraryOverview : Screen
    @Serializable
    data class LibraryDetail(val library: Library) : Screen

    @Serializable
    @OptIn(ExperimentalUuidApi::class)
    data class Reader(val bookId: Uuid) : Screen
    @Serializable
    data object Settings : Screen
}
