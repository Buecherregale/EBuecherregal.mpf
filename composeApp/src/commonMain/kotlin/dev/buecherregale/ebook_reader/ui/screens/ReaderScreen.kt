@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.buecherregale.ebook_reader.ui.components.DictionaryPopup
import dev.buecherregale.ebook_reader.ui.components.ReaderBottomControls
import dev.buecherregale.ebook_reader.ui.components.ReaderTopBar
import dev.buecherregale.ebook_reader.ui.components.rememberPopupState
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.viewmodel.ReaderViewModel
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onToggleMenu: () -> Unit,
) {
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.initState()
    }
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = uiState.isMenuVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                ReaderTopBar(viewModel.uiState.value.title) { navigator.pop() }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.isMenuVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ReaderBottomControls(
                    currentProgress = { uiState.progress },
                    onPageChange = { /* No-op for now */ },
                    onNextChapter = { viewModel.nextChapter() },
                    onPreviousChapter = { viewModel.previousChapter() }
                )
            }
        }
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val popupState = rememberPopupState()
                
                LaunchedEffect(uiState.chapterIdx) {
                    popupState.dismiss()
                }

                ChapterView(
                    bookId = uiState.book.id,
                    chapter = uiState.dom!!.chapter[uiState.chapterIdx],
                    selectedRange = popupState.selectedRange,
                    selectedBlockId = popupState.selectedBlockId,
                    onToggleMenu = onToggleMenu,
                    onSelected = { selectedText, blockId -> popupState.show(selectedText, blockId, uiState.book.metadata.language) }
                )
                uiState.dictionary?.let {
                    DictionaryPopup(state = popupState, dictionary = it)
                }
            }
        }
    }
}