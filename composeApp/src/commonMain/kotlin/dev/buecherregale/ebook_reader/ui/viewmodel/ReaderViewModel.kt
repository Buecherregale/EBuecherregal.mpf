package dev.buecherregale.ebook_reader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.dom.DomDocument
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
    private val bookService: BookService,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState(book = book, isLoading = true))
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun initState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, progress = book.progress) }
            val dom = bookService.open(book.id)
            val language = book.metadata.language
            val dictionary = settingsManager.state.activeDictionaries[language]
            _uiState.update { it.copy(title = book.metadata.title, isLoading = false, dom = dom, dictionary = dictionary) }
        }
    }

    fun toggleMenu() {
        _uiState.update { it.copy(isMenuVisible = !it.isMenuVisible) }
    }

    fun updateProgress() {
        val newProgress = uiState.value.chapterIdx.toDouble() / uiState.value.dom!!.chapter.lastIndex
        viewModelScope.launch {
            bookService.updateProgress(book, newProgress)
            _uiState.update { it.copy(progress = newProgress) }
        }
     }

    fun nextChapter() {
        _uiState.update { state ->
            state.dom?.let { dom ->
                if (state.chapterIdx < dom.chapter.lastIndex) {
                    state.copy(
                        chapterIdx = state.chapterIdx + 1,
                    )
                } else state
            } ?: state
        }
        updateProgress()
    }

    fun previousChapter() {
        _uiState.update { state ->
            if (state.chapterIdx > 0) {
                state.copy(
                    chapterIdx = state.chapterIdx - 1,
                )
            } else state
        }
        updateProgress()
    }
}

data class ReaderUiState(
    val title: String = "",
    val progress: Double = 0.0,
    val isMenuVisible: Boolean = true,
    val isLoading: Boolean = false,
    val book: Book,
    var dom: DomDocument? = null,
    var chapterIdx: Int = 0,
    val dictionary: dev.buecherregale.ebook_reader.core.domain.Dictionary? = null
)