package dev.buecherregale.ebook_reader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.buecherregale.ebook_reader.core.domain.Book
import dev.buecherregale.ebook_reader.core.service.BookService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class ReaderViewModel(
    private val book: Book,
    private val bookService: BookService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateProgress(book.progress)
            _uiState.update { it.copy(title = book.metadata.title) }
        }
    }

    fun toggleMenu() {
        _uiState.update { it.copy(isMenuVisible = !it.isMenuVisible) }
    }

    fun updateProgress(progress: Double) {
        viewModelScope.launch {
            bookService.updateProgress(book, progress)
            _uiState.update { it.copy(progress = progress) }
        }
     }
}

data class ReaderUiState(
    val title: String = "",
    val progress: Double = 0.0,
    val isMenuVisible: Boolean = true,
    val isLoading: Boolean = false
)