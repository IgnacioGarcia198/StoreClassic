package com.garcia.ignacio.storeclassic.data

import com.garcia.ignacio.domain.models.Product
import com.garcia.ignacio.storeclassic.data.remote.StoreClient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductsRepository @Inject constructor(
    private val storeClient: StoreClient
) {
    suspend fun getProducts(): Flow<List<Product>> = storeClient.getProducts()
}