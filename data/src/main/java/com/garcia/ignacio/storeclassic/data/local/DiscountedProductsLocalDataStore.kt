package com.garcia.ignacio.storeclassic.data.local

import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import kotlinx.coroutines.flow.Flow

interface DiscountedProductsLocalDataStore {
    fun findDiscountedProducts(productCodes: Set<String>): Flow<List<DiscountedProduct>>
}