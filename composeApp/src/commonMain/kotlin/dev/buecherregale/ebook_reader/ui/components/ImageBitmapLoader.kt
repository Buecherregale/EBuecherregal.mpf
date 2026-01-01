package dev.buecherregale.ebook_reader.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap

class ImageCache {
    private val map = mutableMapOf<Any, ImageBitmap>()
    fun get(key: Any): ImageBitmap? = map[key]
    fun put(key: Any, bitmap: ImageBitmap) {
        map[key] = bitmap
    }
}

val imageCache = ImageCache()


@Composable
fun <T: Any> rememberImageBitmap(
    key: T,
    bitmapLoader: suspend (T) -> ByteArray?
): State<ImageBitmap?> {
    return produceState(initialValue = null, key1 = key) {
        val cached = imageCache.get(key)
        if (cached != null) {
            value = cached
        } else {
            val bitmap = bitmapLoader(key)?.decodeToImageBitmap()
            value = bitmap
            bitmap?.let { imageCache.put(key, it) }
        }
    }
}