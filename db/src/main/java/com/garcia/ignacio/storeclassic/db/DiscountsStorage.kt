package com.garcia.ignacio.storeclassic.db

import com.garcia.ignacio.storeclassic.data.local.DiscountsLocalDataStore
import com.garcia.ignacio.storeclassic.db.dao.DiscountDao
import com.garcia.ignacio.storeclassic.db.models.toDbDiscount
import com.garcia.ignacio.storeclassic.domain.models.Discount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiscountsStorage @Inject constructor(
    private val discountDao: DiscountDao
) : DiscountsLocalDataStore {
    override fun getAllDiscounts(): Flow<List<Discount>> = discountDao.getAll().map { discounts ->
        discounts.map { it.toDomain() }
    }

    override fun updateDiscounts(discounts: List<Discount>) {
        discountDao.insertAll(discounts.map { it.toDbDiscount() })
    }
}