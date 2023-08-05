package com.garcia.ignacio.storeclassic.network.client

import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.network.models.ProductsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val PRODUCTS_ENDPOINT =
    "https://gist.githubusercontent.com/palcalde/6c19259bd32dd6aafa327fa557859c2f/raw/ba51779474a150ee4367cda4f4ffacdcca479887/Products.json"

class CabifyStoreClient @Inject constructor(
    clientFactory: StoreHttpClientFactory
) : StoreClient {
    private val httpClient: HttpClient by lazy {
        clientFactory.createStoreHttpClient()
    }

    override fun getProducts(): Flow<List<Product>> = flowOf(
        Json.decodeFromString<ProductsResponse>(
            runBlocking { httpClient.get(PRODUCTS_ENDPOINT).body() }
        ).products.map { it.toDomain() }
    )
}