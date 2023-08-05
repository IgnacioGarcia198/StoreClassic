package com.garcia.ignacio.storeclassic.data.remote

import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.coroutines.flow.Flow

interface StoreClient {
    fun getProducts(): Flow<List<Product>>
}