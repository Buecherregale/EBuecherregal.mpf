package dev.buecherregale.ebook_reader

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform