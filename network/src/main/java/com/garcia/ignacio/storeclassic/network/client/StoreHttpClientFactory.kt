package com.garcia.ignacio.storeclassic.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import timber.log.Timber
import javax.inject.Inject

private const val CONNECTION_TIMEOUT = 60_000L
private const val REQUEST_TIMEOUT = 3000L

class StoreHttpClientFactory @Inject constructor(
    private val engine: HttpClientEngine
) {
    fun createStoreHttpClient(): HttpClient = HttpClient(engine) {
        expectSuccess = true

        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT
            connectTimeoutMillis = CONNECTION_TIMEOUT
            socketTimeoutMillis = CONNECTION_TIMEOUT
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag("Logger Ktor =>").v(message)
                }

            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                Timber.tag("HTTP status:").d("${response.status.value}")
            }
        }

        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}