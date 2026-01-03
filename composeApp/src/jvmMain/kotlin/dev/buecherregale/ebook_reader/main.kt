package dev.buecherregale.ebook_reader

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.materialkolor.ktx.themeColors
import com.materialkolor.ktx.toHex
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.uuid.ExperimentalUuidApi

fun calculateSeedColor(bitmap: ImageBitmap): Color {
    val suitableColors = bitmap.themeColors(fallback = Color.Blue)
    suitableColors.forEach { color -> println(color.toHex()) }
    return suitableColors.first()
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalUuidApi::class)
fun main() = application {
    java.nio.file.Path.of("/home/david/Pictures/Wallpapers/miku-redhair.jpg")
        .toFile().inputStream().use {
            calculateSeedColor(it.readAllBytes().decodeToImageBitmap())
        }
    Window(
        onCloseRequest = ::exitApplication,
        title = "ebook_reader",
    ) {
        App()
    }
}