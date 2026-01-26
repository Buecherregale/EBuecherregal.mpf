package dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict

import androidx.compose.ui.text.intl.Locale
import dev.buecherregale.ebook_reader.core.domain.Dictionary
import dev.buecherregale.ebook_reader.core.domain.DictionaryEntry
import dev.buecherregale.ebook_reader.core.language.dictionaries.DictionaryImporter
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.JMDict
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.JMEntry
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.KanjiElement
import dev.buecherregale.ebook_reader.core.language.dictionaries.jmdict.xml.ReadingElement
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.util.HttpUtil
import io.ktor.http.Url
import io.ktor.utils.io.ioDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class JMDictImporter(
    private val fileService: FileService,
): DictionaryImporter {

    override suspend fun importFromFile(
        file: FileRef,
        targetLanguage: Locale
    ): Dictionary = withContext(ioDispatcher()) {
        val xml = fileService.read(file)
        val jmdict = xmlParser.decodeFromString<JMDict>(xml)
        val map = HashMap<String, DictionaryEntry>()
        jmdict.entries
            .mapNotNull { jMEntry -> jMEntry.toDictionaryEntry(localeToGlossLang(targetLanguage)) }
            .forEach { entry ->  map[entry.word] = entry }

        Dictionary(
            id = Uuid.generateV4(),
            name = this@JMDictImporter.dictionaryName,
            originalLanguage = this@JMDictImporter.language,
            targetLanguage = targetLanguage,
            entries = map,
        )
    }

    override suspend fun download(language: Locale): Dictionary = withContext(ioDispatcher()) {
        val glossLang = localeToGlossLang(language)
        val entries = mutableMapOf<String, DictionaryEntry>()
        val tempFile = fileService.getAppDirectory(AppDirectory.DATA).resolve("jmdict_temp.gz")

        try {
            HttpUtil.downloadStream(Url(CURRENT_DOWNLOAD_URI)) { gz ->
                fileService.copy(gz, tempFile)
            }

            val fileSource = fileService.open(tempFile)
            try {
                val xml = fileService.ungzip(fileSource)
                val reader = fileService.streamXml(xml)
                try {
                    while (reader.hasNext()) {
                        when (reader.next()) {
                            EventType.START_ELEMENT -> {
                                if (reader.localName == "entry") {
                                    xmlParser.decodeFromReader<JMEntry>(reader)
                                        .toDictionaryEntry(glossLang)?.let {
                                            entries[it.word] = it
                                        }
                                }
                            }
                            else -> {}
                        }
                    }
                } finally {
                    reader.close()
                }
            } finally {
                fileSource.close()
            }
        } finally {
            if (fileService.exists(tempFile)) {
                fileService.delete(tempFile)
            }
        }

        Dictionary(
            id = Uuid.generateV4(),
            name = this@JMDictImporter.dictionaryName,
            originalLanguage = this@JMDictImporter.language,
            targetLanguage = language,
            entries = entries,
        )
    }

    override val dictionaryName: String
        get() = "JMDict"
    override val language: Locale
        get() = Locale("ja")

    private fun JMEntry.toDictionaryEntry(glossLang: String): DictionaryEntry? {
        val word = kanjiElements.firstOrNull()?.keb
            ?: readingElements.firstOrNull()?.reb
            ?: return null

        val reading = readingElements.firstOrNull()?.reb ?: ""

        val glosses = senses
            .asSequence()
            .flatMap { it.glosses }
            .map {
                if (it.lang == null)
                    return@map it.copy(lang = "eng")
                else
                    return@map it
            }
            .filter { it.lang == null || it.lang == glossLang }
            .map { it.text }
            .distinct()
            .take(GLOSS_COUNT_LIMIT)
            .toList()

        if (glosses.isEmpty()) return null

        val partsOfSpeech = senses
            .flatMap { it.partsOfSpeech }
            .filter { it.isNotBlank() }
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

    private companion object {
        const val CURRENT_DOWNLOAD_URI = "http://ftp.edrdg.org/pub/Nihongo//JMdict.gz"
        const val GLOSS_COUNT_LIMIT = 5

        @OptIn(ExperimentalXmlUtilApi::class)
        val xmlParser = XML.v1.invoke {
            policy = DefaultXmlSerializationPolicy {
                unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
            }
        }

        val COMMON_PRIORITIES = setOf(
            "ichi1", "ichi2",
            "news1", "news2"
        )

        fun KanjiElement.isCommon(): Boolean =
            priorities.any { it in COMMON_PRIORITIES || it.startsWith("nf") }

        fun ReadingElement.isCommon(): Boolean =
            priorities.any { it in COMMON_PRIORITIES || it.startsWith("nf") }

        fun localeToGlossLang(locale: Locale) : String {
            val l = locale.language
            return when(l) {
                "ja" -> "jap"
                "en" -> "eng"
                "de" -> "ger"
                "nl" -> "dut"
                else -> ""
            }
        }
    }
}