package dev.buecherregale.ebook_reader.filesystem

import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileMetadata
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.GZIPInputStream

class DesktopFileService(appName: String) : FileService {
    private val configDir: Path =
        Path.of(System.getenv("XDG_CONFIG_HOME"), appName)
    private val stateDir: Path = Path.of(System.getenv("XDG_STATE_HOME"), appName)
    private val dataDir: Path = Path.of(System.getenv("XDG_DATA_HOME"), appName)

    override fun read(file: FileRef): String {
        val path = toPath(file)
        return Files.readString(path, java.nio.charset.StandardCharsets.UTF_8)
    }

    override fun read(directory: AppDirectory, relativeRef: FileRef): String {
        return read(getAppDirectory(directory).resolve(relativeRef))
    }

    override fun open(file: FileRef): Source {
        return Files.newInputStream(toPath(file), java.nio.file.StandardOpenOption.READ)
            .asSource()
            .buffered()
    }

    override fun readZip(file: FileRef): ZipFileRef {
        return DesktopZipFileRef(java.util.zip.ZipFile(toPath(file).toFile()))
    }

    override fun write(file: FileRef, content: String) {
        write(file, content.toByteArray(java.nio.charset.StandardCharsets.UTF_8))
    }

    override fun write(file: FileRef, content: ByteArray) {
        val path = toPath(file)
        Files.createDirectories(path.parent)
        Files.write(
            path,
            content,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    override fun copy(input: Source, target: FileRef) {
        val path = toPath(target)
        Files.createDirectories(path.parent)
        input.use {
            Files.copy(input.asInputStream(), path, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    override fun exists(file: FileRef): Boolean {
        val path = toPath(file)
        return Files.exists(path)
    }

    override fun getMetadata(file: FileRef): FileMetadata {
        val path = toPath(file)
        if (!Files.exists(path)) {
            throw java.io.FileNotFoundException("file $file does not exist")
        }
        val fileName = path.fileName.toString()
        val firstDot = fileName.indexOf('.')
        if (firstDot == -1) {
            return FileMetadata(
                fileName,
                extension = null,
                size = if (Files.isDirectory(path)) 0 else Files.size(path),
                isDirectory = Files.isDirectory(path)
            )
        }
        return FileMetadata(
            name = fileName.take(firstDot),
            extension = fileName.substring(firstDot),
            size = if (Files.isDirectory(path)) 0 else Files.size(path),
            isDirectory = Files.isDirectory(path)
        )
    }

    override fun getAppDirectory(directory: AppDirectory): FileRef {
        val dir = when (directory) {
            AppDirectory.CONFIG -> configDir
            AppDirectory.DATA -> dataDir
            AppDirectory.STATE -> stateDir
        }
        try {
            Files.createDirectories(dir)
        } catch (e: java.io.IOException) {
            throw java.io.UncheckedIOException(e)
        }
        return DesktopFileRef(dir)
    }


    override fun listChildren(fileRef: FileRef): MutableList<FileRef> {
        val path = toPath(fileRef)
        if (!Files.isDirectory(path)) {
            return kotlin.collections.mutableListOf()
        }

        try {
            Files.newDirectoryStream(path).use { stream ->
                val result: MutableList<FileRef> = java.util.ArrayList()
                for (p in stream) {
                    result.add(DesktopFileRef(p))
                }
                return result
            }
        } catch (e: java.io.IOException) {
            throw java.io.UncheckedIOException(e)
        }
    }

    override fun deserializeRef(s: String): FileRef {
        val p = Path.of(s)
        return DesktopFileRef(p)
    }


    override fun serializeRef(ref: FileRef): String {
        return toPath(ref).toAbsolutePath().toString()
    }

    override fun ungzip(bytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPInputStream(ByteArrayInputStream(bytes)).use { gzis ->
            gzis.transferTo(out)
        }
        return out.toByteArray()
    }

    private fun toPath(ref: FileRef): Path {
        require(ref is DesktopFileRef) { "Unsupported FileRef implementation" }
        return ref.path
    }
}
