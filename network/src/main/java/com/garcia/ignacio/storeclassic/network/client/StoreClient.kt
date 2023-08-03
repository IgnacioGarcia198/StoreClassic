package com.garcia.ignacio.storeclassic.network.client

import com.garcia.ignacio.domain.models.Product
import kotlinx.coroutines.flow.Flow

interface StoreClient {
    suspend fun getProducts(): Flow<List<Product>>
}