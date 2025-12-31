package dev.buecherregale.ebook_reader.filesystem

import dev.buecherregale.ebook_reader.core.service.filesystem.AppDirectory
import dev.buecherregale.ebook_reader.core.service.filesystem.FileMetadata
import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.core.service.filesystem.ZipFileRef
import dev.buecherregale.ebook_reader.toPath
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.zip.GZIPInputStream

class DesktopFileService(appName: String) : FileService {

    private val configDir: Path =
        Path.of(System.getenv("XDG_CONFIG_HOME"), appName)
    private val stateDir: Path = Path.of(System.getenv("XDG_STATE_HOME"), appName)
    private val dataDir: Path = Path.of(System.getenv("XDG_DATA_HOME"), appName)

    override fun read(file: FileRef): String {
        return Files.readString(file.toPath())
    }

    override fun read(
        directory: AppDirectory,
        relativeRef: FileRef
    ): String {
        return Files.readString(getAppDirectory(directory).resolve(relativeRef).toPath())
    }

    override fun open(file: FileRef): Source {
        return Files.newInputStream(file.toPath(),StandardOpenOption.READ)
            .asSource()
            .buffered()
    }

    override fun readZip(file: FileRef): ZipFileRef {
        return DesktopZipFileRef(java.util.zip.ZipFile(file.toPath().toFile()))
    }

    override fun write(
        file: FileRef,
        content: String
    ) {
        write(file, content.toByteArray(StandardCharsets.UTF_8))
    }

    override fun write(
        file: FileRef,
        content: ByteArray
    ) {
        val path = file.toPath()
        Files.createDirectories(path.parent)
        Files.write(
            path,
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    override fun copy(
        input: Source,
        target: FileRef
    ) {
        val path = target.toPath()
        Files.createDirectories(path.parent)
        input.use {
            Files.copy(input.asInputStream(), path, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    override fun exists(file: FileRef): Boolean {
        return Files.exists(file.toPath())
    }

    override fun getMetadata(file: FileRef): FileMetadata {
        val path = file.toPath()
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
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
        return FileRef(dir.toString())
    }

    override fun listChildren(fileRef: FileRef): List<FileRef> {
        val path = fileRef.toPath()
        if (!Files.isDirectory(path)) {
            return kotlin.collections.mutableListOf()
        }

        try {
            Files.newDirectoryStream(path).use { stream ->
                val result: MutableList<FileRef> = ArrayList()
                for (p in stream) {
                    result.add(FileRef(p.toString()))
                }
                return result
            }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    override fun deserializeRef(s: String): FileRef {
        return FileRef(s)
    }

    override fun ungzip(bytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        GZIPInputStream(ByteArrayInputStream(bytes)).use { gzis ->
            gzis.transferTo(out)
        }
        return out.toByteArray()
    }
}