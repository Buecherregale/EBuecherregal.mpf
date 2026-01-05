package dev.buecherregale.ebook_reader.ui

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun <T> pickFile(mainType: String, types: List<String>, resultMapper: (File) -> T) : T? {
    val chooser = JFileChooser().apply {
        dialogTitle = "Select $mainType"
        fileFilter = FileNameExtensionFilter(mainType, *types.toTypedArray())
    }

    val result = chooser.showOpenDialog(null)
    if (result != JFileChooser.APPROVE_OPTION) return null

    val file: File = chooser.selectedFile

    return resultMapper(file)
}