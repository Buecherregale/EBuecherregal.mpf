package dev.buecherregale.ebook_reader.core.util

import dev.buecherregale.ebook_reader.asSource
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.Source

object HttpUtil {

    val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
    }

    /**
     * Download the resource behind the uri via an http request with byteArray body handler.
     *
     * @param url the target url
     * @return the received body bytes
     */
    suspend fun download(url: Url): ByteArray {
        return httpClient.get(url).bodyAsBytes()
    }

    /**
     * Download the resource behind the uri via an http request with streaming body handler.
     *
     * @param url the target url
     * @param block the block to execute with the stream
     */
    suspend fun downloadStream(url: Url, block: suspend (Source) -> Unit) {
        httpClient.prepareGet(url).execute { response ->
            val channel: ByteReadChannel = response.bodyAsChannel()
            block(channel.asSource())
        }
    }
}
