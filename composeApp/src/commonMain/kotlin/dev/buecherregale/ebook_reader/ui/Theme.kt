package dev.buecherregale.ebook_reader.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.ktx.rememberThemeColor

@Composable
fun DynamicTheme (
    image: ImageBitmap,
    content: @Composable () -> Unit
){
    val seedColor = rememberThemeColor(image)

    DynamicMaterialTheme(
        seedColor = seedColor,
        content = content,
    )
}