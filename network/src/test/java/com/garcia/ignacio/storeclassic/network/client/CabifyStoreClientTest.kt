package com.garcia.ignacio.storeclassic.network.client

import com.garcia.ignacio.storeclassic.network.models.NetworkProduct
import com.garcia.ignacio.storeclassic.network.models.ProductsResponse
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

class CabifyStoreClientTest {

    @Test
    fun `client retrieves products from network and returns Result_success`() = runBlocking {
        val response = ProductsResponse(
            listOf(
                NetworkProduct(
                    PRODUCT_CODE,
                    PRODUCT_NAME,
                    PRODUCT_PRICE
                )
            )
        )
        val jsonResponse = Json.encodeToString(response)
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }
        val client = createClient(mockEngine)
        assertEquals(Result.success(response.products.map { it.toDomain() }), client.getProducts())
    }

    @Test
    fun `client returns Result_failure if request result is 404 not found`() = runBlocking {
        val response = ProductsResponse(
            listOf(
                NetworkProduct(
                    PRODUCT_CODE,
                    PRODUCT_NAME,
                    PRODUCT_PRICE
                )
            )
        )
        val jsonResponse = Json.encodeToString(response)
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel(jsonResponse),
                status = HttpStatusCode.NotFound,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }
        val client = createClient(mockEngine)
        val result = client.getProducts()
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as ClientRequestException
        assertEquals(HttpStatusCode.NotFound, error.response.status)
    }


    private fun createClient(engine: MockEngine): CabifyStoreClient =
        CabifyStoreClient(StoreHttpClientFactory(engine))
}