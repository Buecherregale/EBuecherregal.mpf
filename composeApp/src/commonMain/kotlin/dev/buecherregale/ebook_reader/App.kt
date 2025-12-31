package dev.buecherregale.ebook_reader

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation3.ui.NavDisplay
import dev.buecherregale.ebook_reader.ui.navigation.Screen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@Preview
@Composable
@OptIn(KoinExperimentalAPI::class)
fun App() {
    KoinApplication(configuration = koinConfiguration(declaration = {
        modules(commonModule, platformModule())
    }), content = {
        var backStack by remember { mutableStateOf(listOf<Screen>(Screen.LibraryOverview)) }
        val entryProvider = koinEntryProvider()
        MaterialTheme {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.dropLast(0) },
                entryProvider = entryProvider
            )
        }
    })
}