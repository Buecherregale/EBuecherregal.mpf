package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.buecherregale.ebook_reader.ui.components.LibraryCard
import dev.buecherregale.ebook_reader.ui.dialog.CreateLibraryDialog
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.navigation.Screen
import dev.buecherregale.ebook_reader.ui.viewmodel.LibraryViewModel
import ebook_reader.composeapp.generated.resources.Res
import ebook_reader.composeapp.generated.resources.add_24px
import ebook_reader.composeapp.generated.resources.settings_24px
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LibraryScreen(
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    val navigator = koinInject<Navigator>()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadLibraries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Libraries") },
                actions = {
                    IconButton(onClick = { navigator.push(Screen.Settings) }) {
                        Icon(painterResource(Res.drawable.settings_24px), contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                val painter = painterResource(Res.drawable.add_24px)
                Icon(painter, contentDescription = "Add Library")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(state.libraries) { library ->
                    LibraryCard(libraryService = koinInject(), library = library)
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateLibraryDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, image ->
                viewModel.createLibrary(name, image?.bytes)
                showCreateDialog = false
            }
        )
    }
}