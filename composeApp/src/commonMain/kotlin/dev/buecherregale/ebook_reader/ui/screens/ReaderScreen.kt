@file:OptIn(ExperimentalUuidApi::class)

package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.buecherregale.ebook_reader.ui.components.DictionaryPopup
import dev.buecherregale.ebook_reader.ui.components.rememberPopupState
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.navigation.Screen
import dev.buecherregale.ebook_reader.ui.viewmodel.ReaderViewModel
import ebook_reader.composeapp.generated.resources.Res
import ebook_reader.composeapp.generated.resources.arrow_back_24px
import ebook_reader.composeapp.generated.resources.arrow_forward_24px
import ebook_reader.composeapp.generated.resources.settings_24px
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class)
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
                TopAppBar(
                    title = { Text(text = uiState.title,
                        style = MaterialTheme.typography.titleMedium)
                            },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(painterResource(Res.drawable.arrow_back_24px), contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navigator.push(Screen.Settings) }) {
                            Icon(painterResource(Res.drawable.settings_24px), contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.isMenuVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Logger.d { "${uiState.progress}" }
                        Text(
                            text = "${(uiState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )

                        LinearProgressIndicator(
                            progress = { uiState.progress.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                                .padding(4.dp)
                                .defaultMinSize(minHeight = 8.dp),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = { viewModel.previousChapter() }) {
                                Icon(
                                    painter = painterResource(Res.drawable.arrow_back_24px),
                                    contentDescription = "Previous Chapter"
                                )
                            }
                            IconButton(onClick = { viewModel.nextChapter() }) {
                                Icon(
                                    painter = painterResource(Res.drawable.arrow_forward_24px),
                                    contentDescription = "Next Chapter"
                                )
                            }
                        }
                    }
                }
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
                    onSelected = { selectedText, blockId -> popupState.show(selectedText, blockId, uiState.book.metadata.language) },
                    onLinkClick = { target -> viewModel.navigateToLink(target) }
                )
                uiState.dictionary?.let {
                    DictionaryPopup(state = popupState, dictionary = it)
                }
            }
        }
    }
}
