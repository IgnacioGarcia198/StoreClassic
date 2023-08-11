package com.garcia.ignacio.storeclassic.data.local

import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.coroutines.flow.Flow

interface ProductsLocalDataStore {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun updateProducts(products: List<Product>)
}