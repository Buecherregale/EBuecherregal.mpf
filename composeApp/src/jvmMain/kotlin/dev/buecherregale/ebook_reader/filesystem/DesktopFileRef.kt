package dev.buecherregale.ebook_reader.filesystem

import dev.buecherregale.ebook_reader.core.service.filesystem.FileRef
import java.nio.file.Path

class DesktopFileRef(val path: Path) : FileRef {

    override fun resolve(other: FileRef): FileRef {
        require(other is DesktopFileRef) { "Incompatible FileRef" }
        return DesktopFileRef(path.resolve(other.path))
    }

    override fun resolve(other: String): FileRef {
        return DesktopFileRef(path.resolve(other))
    }

    override fun toString(): String {
        return path.toString()
    }

    companion object {
        val RESOURCES: DesktopFileRef = DesktopFileRef(Path.of("src", "main", "resources").toAbsolutePath())
    }
}
