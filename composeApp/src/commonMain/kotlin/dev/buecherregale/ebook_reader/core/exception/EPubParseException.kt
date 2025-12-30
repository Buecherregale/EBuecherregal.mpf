package dev.buecherregale.ebook_reader.core.exception

class EPubParseException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(reason: Throwable?) : super(reason)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
