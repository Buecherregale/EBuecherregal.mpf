package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun <T> PickerButton(
    picker: suspend () -> T,
    onPicked: (T) -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Button(
        modifier = modifier,
        onClick = {
            scope.launch {
                val picked = withContext(Dispatchers.Default) {
                    picker()
                }
                onPicked(picked)
            }
        }
    ) {
        Text(text, textAlign = TextAlign.Center)
    }
}
