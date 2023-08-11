package com.garcia.ignacio.storeclassic.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import timber.log.Timber
import javax.inject.Inject

private const val TIMEOUT = 60_000

class StoreHttpClientFactory @Inject constructor() {
    fun createStoreHttpClient(): HttpClient = HttpClient(Android) {
        expectSuccess = true

        engine {
            connectTimeout = TIMEOUT
            socketTimeout = TIMEOUT
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