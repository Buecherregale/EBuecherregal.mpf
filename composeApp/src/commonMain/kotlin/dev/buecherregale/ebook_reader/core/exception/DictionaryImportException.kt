package dev.buecherregale.ebook_reader.core.exception

class DictionaryImportException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
