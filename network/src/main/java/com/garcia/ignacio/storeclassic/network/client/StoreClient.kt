package com.garcia.ignacio.storeclassic.network.client

import com.garcia.ignacio.storeclassic.network.models.NetworkProduct
import kotlinx.coroutines.flow.Flow

interface StoreClient {
    suspend fun getProducts(): Flow<List<NetworkProduct>>
}