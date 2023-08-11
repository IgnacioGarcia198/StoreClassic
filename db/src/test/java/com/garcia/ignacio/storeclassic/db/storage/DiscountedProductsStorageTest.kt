package com.garcia.ignacio.storeclassic.db.storage

import com.garcia.ignacio.storeclassic.db.dao.DiscountedProductDao
import com.garcia.ignacio.storeclassic.db.models.DbDiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

class DiscountedProductsStorageTest {
    private val discountedProductDao: DiscountedProductDao = mockk()
    private val storage = DiscountedProductsStorage(discountedProductDao)

    @Test
    fun `storage uses dao to find or get all discounted products`() = runBlocking {
        every { discountedProductDao.getDiscountedProducts(any()) }
            .returns(flowOf(listOf(testDbDiscountedProduct)))


        val result = storage.findDiscountedProducts(setOf(PRODUCT_CODE))


        verify { discountedProductDao.getDiscountedProducts(setOf(PRODUCT_CODE)) }
        assertEquals(listOf(testDiscountedProduct), result.first())
    }

    @Test
    fun `storage uses dao to get all products with or without discount`() = runBlocking {
        every { discountedProductDao.getAllProductsAndDiscountIfAny() }
            .returns(flowOf(listOf(testDbDiscountedProduct)))


        val result = storage.getAllProductsAndDiscountIfAny()


        verify { discountedProductDao.getAllProductsAndDiscountIfAny() }
        assertEquals(listOf(testDiscountedProduct), result.first())
    }

    private val testDiscountedProduct = DiscountedProduct(
        Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE)
    )
    private val testDbDiscountedProduct = DbDiscountedProduct(
        PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE
    )
}