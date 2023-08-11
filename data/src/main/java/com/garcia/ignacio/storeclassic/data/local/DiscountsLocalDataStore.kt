package com.garcia.ignacio.storeclassic.data.local

import com.garcia.ignacio.storeclassic.domain.models.Discount
import kotlinx.coroutines.flow.Flow

interface DiscountsLocalDataStore {
    fun getAllDiscounts(): Flow<List<Discount>>
    suspend fun updateDiscounts(discounts: List<Discount>)
}