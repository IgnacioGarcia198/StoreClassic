package com.garcia.ignacio.storeclassic.data.remote

import com.garcia.ignacio.domain.models.Product
import kotlinx.coroutines.flow.Flow

interface StoreClient {
    suspend fun getProducts(): Flow<List<Product>>
}