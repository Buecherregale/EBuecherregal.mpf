package dev.buecherregale.ebook_reader.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.PickBook
import dev.buecherregale.ebook_reader.PickedFile
import dev.buecherregale.ebook_reader.ui.components.PickerButton

fun shortenPath(path: String,): String {
    return path.takeLastWhile { it != '/' }
}

@Composable
fun ImportBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var selectedFile by remember { mutableStateOf<PickedFile?>(null)}
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        PickBook {
            if (it != null) {
                selectedFile = it
            }
            showPicker = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Book") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = selectedFile?.let { shortenPath(it.path) } ?: "",
                    placeholder = { Text("No file selected") },
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                    )
                PickerButton(
                    text = "Pick Book",
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = selectedFile != null,
                onClick = { onConfirm(selectedFile!!.path) }
            ) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },)
}