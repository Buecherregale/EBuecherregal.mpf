package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.core.domain.Library
import dev.buecherregale.ebook_reader.ui.components.LibraryCard
import dev.buecherregale.ebook_reader.ui.dialog.CreateLibraryDialog
import dev.buecherregale.ebook_reader.ui.viewmodel.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LibraryScreen(
    viewModel: LibraryViewModel = koinViewModel(),
    onLibraryClick: (Library) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Libraries") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Text("Add library")
                // Icon(Icons.Default.Add, contentDescription = "Add Library")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(state.libraries) { library ->
                LibraryCard(library = library, imageBitmap = viewModel.readImageBytes(library)
                    ?.decodeToImageBitmap(), onClick = { onLibraryClick(library) })
            }
        }
    }

    if (showCreateDialog) {
        CreateLibraryDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, fileRef ->
                viewModel.createLibrary(name, fileRef)
                showCreateDialog = false
            }
        )
    }
}