package com.garcia.ignacio.storeclassic.db.storage

import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.data.local.ProductsLocalDataStore
import com.garcia.ignacio.storeclassic.db.dao.ProductDao
import com.garcia.ignacio.storeclassic.db.models.toDbProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductsStorage @Inject constructor(
    private val productDao: ProductDao
) : ProductsLocalDataStore {
    override fun getAllProducts(): Flow<List<Product>> = productDao.getAll().map { products ->
        products.map { it.toDomain() }
    }

    override suspend fun updateProducts(products: List<Product>) {
        productDao.insertAll(products.map { it.toDbProduct() })
    }
}