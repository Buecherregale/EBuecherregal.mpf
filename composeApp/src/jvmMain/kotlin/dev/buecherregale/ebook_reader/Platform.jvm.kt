package dev.buecherregale.ebook_reader

import dev.buecherregale.ebook_reader.core.service.filesystem.FileService
import dev.buecherregale.ebook_reader.filesystem.DesktopFileService
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun platformModule(): Module {
    return module {
        single { DesktopFileService("ebook-reader") } binds arrayOf(FileService::class)
    }
}