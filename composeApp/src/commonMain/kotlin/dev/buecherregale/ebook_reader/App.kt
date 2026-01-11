package dev.buecherregale.ebook_reader

import androidx.compose.runtime.Composable
import androidx.navigation3.ui.NavDisplay
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.formats.dictionaries.jmdict.JMDictImporter
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.navigation.navigationModule
import dev.buecherregale.ebook_reader.ui.theming.ShellTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@Composable
@OptIn(KoinExperimentalAPI::class)
fun App() {
    KoinApplication(
        configuration = koinConfiguration {
            modules(navigationModule, commonModule, platformModule())
        }
    ) {
        registerImplsInFactory()
        ShellTheme {
            NavDisplay(
                backStack = koinInject<Navigator>().backStack,
                entryProvider = koinEntryProvider()
            )
        }
    }
}

fun registerImplsInFactory() {
    DictionaryImporterFactory.register("JmDict", ::JMDictImporter)
}
