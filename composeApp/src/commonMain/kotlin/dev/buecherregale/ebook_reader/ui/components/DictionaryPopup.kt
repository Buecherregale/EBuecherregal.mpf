package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.service.DictionaryService
import dev.buecherregale.ebook_reader.findWordInSelection
import org.koin.compose.koinInject


@Stable
class PopupState {
    var text by mutableStateOf<String?>(null)
    var offset by mutableStateOf(Offset.Zero)
    var selectedRange by mutableStateOf<TextRange?>(null)
    var selectedBlockId by mutableStateOf<String?>(null)

    fun show(selectedText: SelectedText, blockId: String, locale: Locale) {
        val word = findWordInSelection(selectedText, locale) ?: return
        text = selectedText.text.substring(word.start, word.end)
        offset = selectedText.position
        selectedRange = TextRange(word.start, word.end)
        selectedBlockId = blockId
    }

    fun dismiss() {
        text = null
        selectedRange = null
        selectedBlockId = null
    }

    val isVisible: Boolean
        get() = text != null
}

@Composable
fun rememberPopupState(): PopupState =
    remember { PopupState() }

@Composable
fun DictionaryPopup(
    state: PopupState,
    dictionary: Dictionary,
    dictionaryService: DictionaryService = koinInject()
) {
    if (!state.isVisible) return

    Popup(
        offset = state.offset.round(),
        onDismissRequest = { state.dismiss() },
        properties = PopupProperties(
            focusable = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            val entry = dictionaryService.lookup(dictionary, state.text!!).firstOrNull()
            if (entry == null) {
                EntryText("No definition found.")
            } else {
                EntryText(
                    text = entry.reading,
                    style = MaterialTheme.typography.bodySmall
                )
                EntryText(
                    text = entry.meaning,
                    style = MaterialTheme.typography.bodyMedium
                )
                EntryText(
                    text = entry.partsOfSpeech.joinToString(separator = " "),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun EntryText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface
    )
}
