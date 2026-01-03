@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.buecherregale.ebook_reader.ui.components.ReaderBottomControls
import dev.buecherregale.ebook_reader.ui.components.ReaderTopBar
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.viewmodel.ReaderViewModel
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onPageChange: (Int) -> Unit,
    onToggleMenu: () -> Unit,
) {
    val navigator = koinInject<Navigator>()
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = viewModel.uiState.value.isMenuVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                ReaderTopBar(viewModel.uiState.value.title) { navigator.pop() }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = viewModel.uiState.value.isMenuVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ReaderBottomControls({ viewModel.uiState.value.progress }, onPageChange)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onToggleMenu() }
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                ChapterView(
                    bookId = uiState.book.id,
                    chapter = uiState.dom!!.chapter.first()
                )
            }
        }
    }
}