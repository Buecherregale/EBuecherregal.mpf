package dev.buecherregale.ebook_reader

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation3.ui.NavDisplay
import dev.buecherregale.ebook_reader.core.formats.books.BookParserFactory
import dev.buecherregale.ebook_reader.core.formats.books.epub.EPubParser
import dev.buecherregale.ebook_reader.core.formats.dictionaries.DictionaryImporterFactory
import dev.buecherregale.ebook_reader.core.formats.dictionaries.jmdict.JMDictImporter
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.ui.navigation.Navigator
import dev.buecherregale.ebook_reader.ui.navigation.navigationModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

// TODO: let user choose an image in settings, generating fitting theme via  https://github.com/jordond/MaterialKolor
/**
 * TODO:
 * ```
 * @Composable
 * fun DynamicTheme(image: ImageBitmap, content: @Composable () -> Unit) {
 *     val seedColor = rememberThemeColor(image, fallback = MaterialTheme.colorScheme.primary)
 *
 *   DynamicMaterialTheme(
 *         seedColor = seedColor,
 *         content = content
 *     )
 * }
 * ```
 */
@Composable
@OptIn(KoinExperimentalAPI::class)
fun App() {
    KoinApplication(
        configuration = koinConfiguration {
            modules(navigationModule, commonModule, platformModule())
        }
    ) {
        registerImplsInFactory(koinInject())
        MaterialTheme {
            NavDisplay(
                backStack = koinInject<Navigator>().backStack,
                entryProvider = koinEntryProvider()
            )
        }
    }
}

fun registerImplsInFactory(fileService: FileService) {
    DictionaryImporterFactory.register("JmDict", ::JMDictImporter)
    BookParserFactory.register({ EPubParser.isEPub(fileService, it) }, ::EPubParser)
}
