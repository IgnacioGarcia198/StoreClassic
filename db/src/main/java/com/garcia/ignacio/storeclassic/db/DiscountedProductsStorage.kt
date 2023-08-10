package com.garcia.ignacio.storeclassic.db

import com.garcia.ignacio.storeclassic.data.local.DiscountedProductsLocalDataStore
import com.garcia.ignacio.storeclassic.db.dao.DiscountedProductDao
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiscountedProductsStorage @Inject constructor(
    private val discountedProductDao: DiscountedProductDao,
) : DiscountedProductsLocalDataStore {

    override fun findDiscountedProducts(
        productCodes: Set<String>
    ): Flow<List<DiscountedProduct>> =
        discountedProductDao.getDiscountedProducts(productCodes).map { discountedProducts ->
            discountedProducts.map { it.toDomain() }
        }
}