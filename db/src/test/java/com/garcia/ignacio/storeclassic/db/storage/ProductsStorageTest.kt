package com.garcia.ignacio.storeclassic.db.storage

import com.garcia.ignacio.storeclassic.db.dao.ProductDao
import com.garcia.ignacio.storeclassic.db.models.toDbProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

class ProductsStorageTest {
    private val productDao: ProductDao = mockk(relaxed = true)
    private val storage = ProductsStorage(productDao)

    @Test
    fun `storage uses dao to insert products`() = runBlocking {
        storage.updateProducts(listOf(testProduct))


        coVerify { productDao.insertAll(listOf(testProduct.toDbProduct())) }
    }

    @Test
    fun `storage uses dao to retrieve products`() = runBlocking {
        every { productDao.getAll() }.returns(flowOf(listOf(testProduct.toDbProduct())))


        val result = storage.getAllProducts()


        coVerify { productDao.getAll() }
        assertEquals(listOf(testProduct), result.first())
    }

    private val testProduct = Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE)
}