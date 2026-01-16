package dev.buecherregale.ebook_reader.ui.viewmodel

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.domain.DictionaryMetadata
import dev.buecherregale.ebook_reader.core.service.DictionaryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val dictionaryService: DictionaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val supported = dictionaryService.listSupportedDictionaryNames()
            val downloaded = dictionaryService.listDownloadedDictionaryMetadata()
            
            val activeIds = settingsManager.state.activeDictionaries.entries.associate { 
                it.key to it.value.id 
            }

            _uiState.update {
                it.copy(
                    supportedDictionaries = supported,
                    downloadedDictionaries = downloaded,
                    activeDictionaryIds = activeIds,
                    isLoading = false
                )
            }
        }
    }

    fun downloadDictionary(name: String, languageTag: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                dictionaryService.download(name, Locale(languageTag))
                val downloaded = dictionaryService.listDownloadedDictionaryMetadata()
                _uiState.update { it.copy(downloadedDictionaries = downloaded, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Unknown error", isLoading = false) }
            }
        }
    }

    fun setActiveDictionary(originalLanguage: Locale, dictionaryId: Uuid) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            try {
                settingsManager.setActiveDictionary(dictionaryId)
                val activeIds = _uiState.value.activeDictionaryIds.toMutableMap()
                activeIds[originalLanguage] = dictionaryId
                _uiState.update { it.copy(activeDictionaryIds = activeIds) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to set dictionary") }
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            settingsManager.save()
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@OptIn(ExperimentalUuidApi::class)
data class SettingsUiState(
    val supportedDictionaries: List<String> = emptyList(),
    val downloadedDictionaries: List<DictionaryMetadata> = emptyList(),
    val activeDictionaryIds: Map<Locale, Uuid> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
