package dev.buecherregale.ebook_reader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.viewmodel.SettingsViewModel
import ebook_reader.composeapp.generated.resources.Res
import ebook_reader.composeapp.generated.resources.arrow_back_24px
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val navigator = koinInject<Navigator>()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(painterResource(Res.drawable.arrow_back_24px), contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveSettings() },
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = LocalContentColor.current
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Download Dictionary", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    DownloadDictionarySection(
                        supportedDictionaries = state.supportedDictionaries,
                        onDownload = { name, lang -> viewModel.downloadDictionary(name, lang) }
                    )
                }

                item {
                    Text("Active Dictionaries", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }

                val groupedDictionaries = state.downloadedDictionaries.groupBy { it.originalLanguage }
                
                items(groupedDictionaries.keys.toList()) { lang ->
                    val dicts = groupedDictionaries[lang] ?: emptyList()
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Language: ${lang.toLanguageTag()}", style = MaterialTheme.typography.titleSmall)
                            dicts.forEach { dict ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RadioButton(
                                        selected = state.activeDictionaryIds[lang] == dict.id,
                                        onClick = { viewModel.setActiveDictionary(lang, dict.id) }
                                    )
                                    Text("${dict.name} (${dict.targetLanguage})")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDictionarySection(
    supportedDictionaries: List<String>,
    onDownload: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedDictionary by remember { mutableStateOf(if (supportedDictionaries.isNotEmpty()) supportedDictionaries[0] else "") }
    var languageTag by remember { mutableStateOf("") }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                readOnly = true,
                value = selectedDictionary,
                onValueChange = {},
                label = { Text("Dictionary Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                supportedDictionaries.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedDictionary = selectionOption
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = languageTag,
            onValueChange = { languageTag = it },
            label = { Text("Language Tag (e.g. en, de)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(8.dp))
        
        Button(
            onClick = { 
                if (selectedDictionary.isNotEmpty() && languageTag.isNotEmpty()) {
                    onDownload(selectedDictionary, languageTag)
                }
            },
            enabled = selectedDictionary.isNotEmpty() && languageTag.isNotEmpty()
        ) {
            Text("Download")
        }
    }
}
