package com.garcia.ignacio.storeclassic.db.dao

import com.garcia.ignacio.storeclassic.db.database.DatabaseTest
import com.garcia.ignacio.storeclassic.db.models.DbProduct
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

class ProductDaoTest : DatabaseTest() {
    private lateinit var productDao: ProductDao

    @Before
    fun setUp() {
        productDao = db.productDao
    }

    @Test
    fun `insertAll() inserts a list of products that can be retrieved`() = runBlocking {
        val products = (1..10).map { testProduct("$PRODUCT_CODE$it") }
        productDao.insertAll(products)


        assertEquals(products, productDao.getAll().first())
    }

    @Test
    fun `insertAll() replaces products with same product code`() = runBlocking {
        val products = (1..10).map { testProduct() }
        productDao.insertAll(products)


        assertEquals(listOf(testProduct()), productDao.getAll().first())
    }

    private fun testProduct(
        code: String = PRODUCT_CODE,
        name: String = PRODUCT_NAME,
        price: Double = PRODUCT_PRICE,
    ): DbProduct = DbProduct(code, name, price)
}