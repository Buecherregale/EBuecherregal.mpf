package dev.buecherregale.ebook_reader.core.util

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object ImportUtil {

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
}
