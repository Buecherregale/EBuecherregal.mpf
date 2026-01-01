package dev.buecherregale.ebook_reader.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import dev.buecherregale.ebook_reader.PickedImage
import dev.buecherregale.ebook_reader.pickImage
import dev.buecherregale.ebook_reader.ui.components.PickerButton

@Composable
fun CreateLibraryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, PickedImage?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<PickedImage?>(null)}

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Library") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Library Name") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = image?.name ?: "No Image selected",
                        onValueChange = {},
                        label = { Text("Cover Image") },
                        readOnly = true
                    )
                    PickerButton(
                        text = "Pick Image",
                        picker = { pickImage() },
                        onPicked = { image = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, image) }
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}