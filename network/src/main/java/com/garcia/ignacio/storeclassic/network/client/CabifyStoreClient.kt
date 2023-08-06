package com.garcia.ignacio.storeclassic.network.client

import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.domain.models.ProductCode
import com.garcia.ignacio.storeclassic.network.models.NetworkDiscount
import com.garcia.ignacio.storeclassic.network.models.ProductsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override fun getProducts(): Flow<List<Product>> = flow {
        emit(
            Json.decodeFromString<ProductsResponse>(
                httpClient.get(PRODUCTS_ENDPOINT).body()
            ).products.map { it.toDomain() }
        )
    }

    override fun getDiscounts(): Flow<List<Discount>> = flow {
        emit(
            listOf(
                NetworkDiscount(
                    type = Discount.XForY.TYPE,
                    productCode = ProductCode.VOUCHER.name,
                    params = listOf(2.0, 1.0)
                ),
                NetworkDiscount(
                    type = Discount.BuyInBulk.TYPE,
                    productCode = ProductCode.TSHIRT.name,
                    params = listOf(3.0, 5.0)
                )
            ).map { it.toDomain() }
        )
    }
}