package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import dev.buecherregale.ebook_reader.core.language.findWordUAX29


@Stable
class PopupState {
    var text by mutableStateOf<String?>(null)
    var offset by mutableStateOf(Offset.Zero)
    var selectedRange by mutableStateOf<TextRange?>(null)
    var selectedBlockId by mutableStateOf<String?>(null)

    fun show(selectedText: SelectedText, blockId: String) {
        val word = findWordUAX29(selectedText.text, selectedText.index)
            ?: return
        text = word.word
        offset = selectedText.position
        selectedRange = TextRange(word.start, word.endExclusive)
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
    state: PopupState
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
        Text(
            text = state.text.orEmpty(),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
