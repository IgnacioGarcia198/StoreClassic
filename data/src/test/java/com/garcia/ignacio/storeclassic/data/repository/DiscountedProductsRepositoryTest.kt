package com.garcia.ignacio.storeclassic.data.repository

import com.garcia.ignacio.storeclassic.data.exceptions.ErrorHandler
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.data.local.DiscountedProductsLocalDataStore
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0

class DiscountedProductsRepositoryTest {
    private val localDataStore: DiscountedProductsLocalDataStore = mockk()
    private val errorHandler: ErrorHandler = mockk(relaxed = true)
    private val repository = DiscountedProductsRepository(
        localDataStore, errorHandler
    )
    private val discountedProducts = listOf(
        DiscountedProduct(
            Product(PRODUCT_CODE, PRODUCT_NAME, PRODUCT_PRICE),
            Discount.XForY(PRODUCT_CODE, 2, 3)
        )
    )

    @Test
    fun `findDiscountedProducts() uses local datastore to find discounted products for product codes`() =
        runBlocking {
            every { localDataStore.findDiscountedProducts(any()) }.returns(
                flowOf(discountedProducts)
            )


            val result =
                repository.findDiscountedProducts((1..4).map { "$PRODUCT_CODE$it" }.toSet())


            verify {
                localDataStore.findDiscountedProducts((1..4).map { "$PRODUCT_CODE$it" }.toSet())
            }
            assertEquals(discountedProducts, result.first())
        }

    @Test
    fun `findDiscountedProducts() reports errors using ErrorReporter`() =
        runTest {
            every { localDataStore.findDiscountedProducts(any()) }.returns(
                flow {
                    throw TestException()
                }
            )


            val result =
                repository.findDiscountedProducts((1..4).map { "$PRODUCT_CODE$it" }.toSet())


            assertEquals(emptyList<DiscountedProduct>(), result.first())
            val slot = slot<List<Throwable>>()
            verify { errorHandler.handleErrors(capture(slot)) }
            val error = slot.captured.first() as StoreException.ErrorRetrievingDiscountedProducts
            assertEquals("test", error.message)
            assertEquals(TestException(), error.cause)
        }

    @Test
    fun `getAllProductsAndDiscountIfAny() uses local datastore to find discounted products for product codes`() =
        runBlocking {
            every { localDataStore.getAllProductsAndDiscountIfAny() }.returns(
                flowOf(discountedProducts)
            )


            val result = repository.getAllProductsWithDiscountsIfAny()


            verify {
                localDataStore.getAllProductsAndDiscountIfAny()
            }
            assertEquals(discountedProducts, result.first())
        }

    @Test
    fun `getAllProductsAndDiscountIfAny() reports errors using ErrorReporter`() =
        runTest {
            every { localDataStore.getAllProductsAndDiscountIfAny() }.returns(
                flow {
                    throw TestException()
                }
            )


            val result =
                repository.getAllProductsWithDiscountsIfAny()


            assertEquals(emptyList<DiscountedProduct>(), result.first())
            val slot = slot<List<Throwable>>()
            verify { errorHandler.handleErrors(capture(slot)) }
            val error = slot.captured.first() as StoreException.ErrorRetrievingDiscountedProducts
            assertEquals("test", error.message)
            assertEquals(TestException(), error.cause)
        }
}