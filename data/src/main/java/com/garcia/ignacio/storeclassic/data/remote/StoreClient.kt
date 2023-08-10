package com.garcia.ignacio.storeclassic.data.remote

import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.coroutines.flow.Flow

interface StoreClient {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getDiscounts(): Result<List<Discount>>
}