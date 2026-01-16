package dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.domain.DictionaryEntry
import dev.buecherregale.ebook_reader.core.language.dictionaries.DictionaryImporter
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.JMDict
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.JMEntry
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.KanjiElement
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.ReadingElement
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.ImportUtil
import io.ktor.http.Url
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML1_0
import nl.adaptivity.xmlutil.serialization.XmlConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class JMDictImporter(private val fileService: FileService) : DictionaryImporter {

    override suspend fun importFromFile(file: FileRef, targetLanguage: Locale): Dictionary {
        val entries = parse(fileService.read(file), mapLocale(targetLanguage))

        return Dictionary(
            id = Uuid.generateV4(),
            name = this.dictionaryName,
            originalLanguage = this.language,
            targetLanguage = targetLanguage,
            entries = entries,
        )
    }

    override suspend fun download(): ByteArray {
        var data: ByteArray =
            ImportUtil.download(Url(CURRENT_DOWNLOAD_URI))
        data = fileService.ungzip(data)

        return data
    }

    override suspend fun download(language: Locale): Dictionary {
        var data: ByteArray =
            ImportUtil.download(Url(CURRENT_DOWNLOAD_URI))
        data = fileService.ungzip(data)
        val entries = parse(data.decodeToString(), mapLocale(language))
        return Dictionary(
            id = Uuid.generateV4(),
            name = this.dictionaryName,
            originalLanguage = this.language,
            targetLanguage = language,
            entries = entries,
        )
    }

    override val dictionaryName: String = "JmDict"
    override val language: Locale = Locale("ja")

    private fun mapLocale(locale: Locale) : String {
        val l = locale.language
        return when(l) {
            "ja" -> "jap"
            "en" -> "eng"
            "de" -> "ger"
            "nl" -> "dut"
            else -> ""
        }
    }

    // dict parsing from xml
    private fun parse(xml: String, glossLang: String): Map<String, DictionaryEntry> {
        val jmdict = xmlParser.decodeFromString<JMDict>(xml)
        val map = HashMap<String, DictionaryEntry>()
        jmdict.entries
            .map {jMEntry -> jMEntry.toDictionaryEntry(glossLang) }
            .forEach { entry ->  map[entry!!.word] = entry }
        return map
    }

    fun JMEntry.toDictionaryEntry(glossLang: String): DictionaryEntry? {
        val word = kanjiElements.firstOrNull()?.keb
            ?: readingElements.firstOrNull()?.reb
            ?: return null

        val reading = readingElements.firstOrNull()?.reb ?: ""

        val glosses = senses
            .flatMap { it.glosses }
            .filter { it.lang == null || it.lang == glossLang }
            .map { it.text }
            .distinct()

        if (glosses.isEmpty()) return null

        val partsOfSpeech = senses
            .flatMap { it.partsOfSpeech }
            .distinct()

        val common = (kanjiElements.any { it.isCommon() }
                || readingElements.any { it.isCommon() })

        return DictionaryEntry(
            word = word,
            reading = reading,
            meaning = glosses.joinToString("; "),
            partsOfSpeech = partsOfSpeech,
            common = common
        )
    }

    companion object {
        private const val CURRENT_DOWNLOAD_URI = "http://ftp.edrdg.org/pub/Nihongo//JMdict.gz"

        @OptIn(ExperimentalXmlUtilApi::class)
        private val xmlParser = XML1_0.recommended {
            policy = DefaultXmlSerializationPolicy {
                unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
                isStrictBoolean = false
                isStrictAttributeNames = false
                isStrictOtherAttributes = false
            }
        }

        private val COMMON_PRIORITIES = setOf(
            "ichi1", "ichi2",
            "news1", "news2"
        )

        private fun KanjiElement.isCommon(): Boolean =
            priorities.any { it in COMMON_PRIORITIES || it.startsWith("nf") }

        private fun ReadingElement.isCommon(): Boolean =
            priorities.any { it in COMMON_PRIORITIES || it.startsWith("nf") }
    }
}