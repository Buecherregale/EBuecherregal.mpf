package dev.buecherregale.ebook_reader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.domain.Library
import dev.buecherregale.ebook_reader.core.service.BookService
import dev.buecherregale.ebook_reader.core.service.LibraryService
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class LibraryDetailViewModel(
    private val library: Library,
    private val libraryService: LibraryService,
    private val bookService: BookService
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryDetailUiState(library))
    val uiState = _uiState.asStateFlow()

    init {
        loadBooks()
    }

     fun loadBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val books = library.bookIds.map { id ->
                bookService.readData(id)
            }

            _uiState.update { it.copy(books = books, isLoading = false) }
        }
    }

    fun importBook(path: String) {
        val ref = FileRef(path)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val book = bookService.importBook(ref)
            libraryService.addBook(library.id, book.id)

            val books = _uiState.value.books.toMutableList()
            books.add(book)

            _uiState.update { it.copy(books = books, isLoading = false) }
        }
    }
}

data class LibraryDetailUiState(
    var library: Library,
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false
)
