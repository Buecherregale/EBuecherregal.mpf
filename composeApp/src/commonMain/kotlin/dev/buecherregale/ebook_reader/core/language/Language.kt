package dev.buecherregale.ebook_reader.core.language

import androidx.compose.ui.text.intl.Locale
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Normalize different language representations to the locale format
 */
fun normalizeLanguage(raw: String): Locale {
    val cleaned = raw
        .replace('_', '-')
        .trim()

    // special case for legacy
    val fixed = when (cleaned.lowercase()) {
        "jp" -> "ja"
        "cn" -> "zh-CN"
        "tw" -> "zh-TW"
        "ger" -> "de"
        else -> cleaned
    }

    return Locale(fixed)
}

object LocaleSerializer : KSerializer<Locale> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Locale", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Locale) {
        encoder.encodeString(value.toLanguageTag())
    }

    override fun deserialize(decoder: Decoder): Locale {
        return Locale(decoder.decodeString())
    }
}
