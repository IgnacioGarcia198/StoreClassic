package com.garcia.ignacio.storeclassic.network.client

import com.garcia.ignacio.storeclassic.network.models.NetworkProduct

interface StoreClient {
    suspend fun getProducts(): Result<List<NetworkProduct>>
}