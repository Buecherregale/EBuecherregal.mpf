package dev.buecherregale.ebook_reader

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.DelicateCoroutinesApi
import org.jetbrains.compose.resources.painterResource
import kotlin.uuid.ExperimentalUuidApi

@OptIn(DelicateCoroutinesApi::class, ExperimentalUuidApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = APP_NAME,
        icon = painterResource(icon())
    ) {
        App()
    }
}