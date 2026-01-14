package dev.buecherregale.ebook_reader.core.language

data class WordSpan(val word: String, val start: Int, val endExclusive: Int)

/**
 * Finds the next complete word starting at the index.
 * If the index is part of a word, the function will look left and right to find the word boundaries,
 * meaning index is part of the [WordSpan] interval.
 *
 * If no word is found, for example because the index is not part of a word, `null` is returned.
 *
 * Word boundaries are defined by loosely following [UAX #29](https://www.unicode.org/reports/tr29).
 *
 * @param text The text containing the index
 * @param index The index where to look for a word in `text`
 *
 * @return the word if found or `null`
 */
fun findWordUAX29(text: String, index: Int): WordSpan? {
    if (index !in text.indices) return null

    val ch = text[index]
    if (!ch.isWordChar() && !ch.isCombiningMark()) return null

    var start = index
    var end = index + 1

    // Expand left
    while (start > 0) {
        val prev = text[start - 1]
        val curr = text[start]
        val prevPrev = if (start - 2 >= 0) text[start - 2] else null

        if (prev.isWordContinuation(prevPrev, curr)) {
            start--
        } else {
            break
        }
    }

    // Expand right
    while (end < text.length) {
        val curr = text[end]
        val prev = text[end - 1]
        val next = if (end + 1 < text.length) text[end + 1] else null

        if (curr.isWordContinuation(prev, next)) {
            end++
        } else {
            break
        }
    }

    return WordSpan(
        word = text.substring(start, end),
        start = start,
        endExclusive = end
    )
}


private fun Char.isWordContinuation(prev: Char?, next: Char?): Boolean {
    if (isWordChar() || isCombiningMark()) return true

    // WB6/WB7: allow apostrophes inside words (e.g., can't)
    if (isApostrophe()) {
        return prev?.isWordChar() == true && next?.isWordChar() == true
    }

    return false
}


private fun Char.isWordChar(): Boolean =
    when (this.category) {
        CharCategory.UPPERCASE_LETTER,
        CharCategory.LOWERCASE_LETTER,
        CharCategory.TITLECASE_LETTER,
        CharCategory.MODIFIER_LETTER,
        CharCategory.OTHER_LETTER,
        CharCategory.DECIMAL_DIGIT_NUMBER -> true
        else -> false
    }

private fun Char.isCombiningMark(): Boolean =
    when (this.category) {
        CharCategory.NON_SPACING_MARK,
        CharCategory.COMBINING_SPACING_MARK,
        CharCategory.ENCLOSING_MARK -> true
        else -> false
    }

private fun Char.isApostrophe(): Boolean =
    this == '\'' || this == '’'
