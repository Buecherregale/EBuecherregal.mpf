package dev.buecherregale.ebook_reader

import org.koin.core.module.Module

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun platformModule(): Module