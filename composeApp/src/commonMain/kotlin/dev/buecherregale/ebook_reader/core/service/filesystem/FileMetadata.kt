package dev.buecherregale.ebook_reader.core.service.filesystem

data class FileMetadata(val name: String, val extension: String?, val isDirectory: Boolean, val size: Long)
