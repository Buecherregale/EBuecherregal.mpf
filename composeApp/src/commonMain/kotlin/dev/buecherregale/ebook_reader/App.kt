package dev.buecherregale.ebook_reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.ui.NavDisplay
import dev.buecherregale.ebook_reader.core.config.SettingsManager
import dev.buecherregale.ebook_reader.core.language.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.JMDictImporter
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.navigation.navigationModule
import dev.buecherregale.ebook_reader.ui.theming.ShellTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

const val APP_NAME = "EBuecherregal"

@Composable
@OptIn(KoinExperimentalAPI::class)
fun App() {
    KoinApplication(
        configuration = koinConfiguration {
            modules(navigationModule, commonModule, platformModule())
        }
    ) {
        registerImplsInFactory()

        val settingsManager = koinInject<SettingsManager>()
        LaunchedEffect(Unit) {
            settingsManager.loadOrCreate()
        }

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
