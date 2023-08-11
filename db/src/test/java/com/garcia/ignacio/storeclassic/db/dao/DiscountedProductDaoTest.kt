package com.garcia.ignacio.storeclassic.db.dao

import com.garcia.ignacio.storeclassic.db.database.DatabaseTest
import com.garcia.ignacio.storeclassic.db.models.DbDiscount
import com.garcia.ignacio.storeclassic.db.models.DbDiscountedProduct
import com.garcia.ignacio.storeclassic.db.models.DbProduct
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0
private const val DISCOUNT_TYPE = "type"

class DiscountedProductDaoTest : DatabaseTest() {
    private lateinit var dao: DiscountedProductDao
    private lateinit var productDao: ProductDao
    private lateinit var discountDao: DiscountDao

    @Before
    fun setUp() {
        dao = db.discountedProductDao
        productDao = db.productDao
        discountDao = db.discountDao
    }

    @Test
    fun `getDiscountedProducts() with empty set finds all products that have discount`() =
        runBlocking {
            val products = (1..10).map { testProduct("$PRODUCT_CODE$it") }
            productDao.insertAll(products)
            val discounts = (1..10).map { testDiscount(code = "$PRODUCT_CODE$it") }
            discountDao.insertAll(discounts)

            val expectedDiscountedProducts = products.mapIndexed { index, dbProduct ->
                val discount = discounts[index]
                DbDiscountedProduct(
                    dbProduct.code,
                    dbProduct.name,
                    dbProduct.price,
                    discount.type,
                    discount.params
                )
            }


            assertEquals(expectedDiscountedProducts, dao.getDiscountedProducts(emptySet()).first())
        }

    @Test
    fun `getDiscountedProducts() does not return products that have no discount`() = runBlocking {
        val products = (1..10).map { testProduct("$PRODUCT_CODE$it") }
        productDao.insertAll(products)
        val discounts = (1..8).map { testDiscount(code = "$PRODUCT_CODE$it") }
        discountDao.insertAll(discounts)

        val expectedDiscountedProducts = discounts.mapIndexed { index, dbDiscount ->
            val dbProduct = products[index]
            DbDiscountedProduct(
                dbProduct.code,
                dbProduct.name,
                dbProduct.price,
                dbDiscount.type,
                dbDiscount.params
            )
        }


        assertEquals(expectedDiscountedProducts, dao.getDiscountedProducts(emptySet()).first())
    }

    @Test
    fun `getDiscountedProducts() with a non-empty set returns the discounts for the set`() =
        runBlocking {
            val products = (1..10).map { testProduct("$PRODUCT_CODE$it") }
            productDao.insertAll(products)
            val discounts = (1..10).map { testDiscount(code = "$PRODUCT_CODE$it") }
            discountDao.insertAll(discounts)

            val expectedDiscountedProducts = discounts.mapIndexed { index, dbDiscount ->
                val dbProduct = products[index]
                DbDiscountedProduct(
                    dbProduct.code,
                    dbProduct.name,
                    dbProduct.price,
                    dbDiscount.type,
                    dbDiscount.params
                )
            }.take(3)


            assertEquals(
                expectedDiscountedProducts,
                dao.getDiscountedProducts(products.take(3).map { it.code }.toSet()).first()
            )
        }

    @Test
    fun `getAllProductsAndDiscountIfAny() returns null discount values for products that have no discount`() =
        runBlocking {
            val products = (1..10).map { testProduct("$PRODUCT_CODE$it") }
            productDao.insertAll(products)
            val discounts = (1..8).map { testDiscount(code = "$PRODUCT_CODE$it") }
            discountDao.insertAll(discounts)

            val expectedDiscountedProducts = discounts.mapIndexed { index, dbDiscount ->
                val dbProduct = products[index]
                DbDiscountedProduct(
                    dbProduct.code,
                    dbProduct.name,
                    dbProduct.price,
                    dbDiscount.type,
                    dbDiscount.params
                )
            } + products[8].toDbDiscountedProduct(null) + products[9].toDbDiscountedProduct(null)


            assertEquals(expectedDiscountedProducts, dao.getAllProductsAndDiscountIfAny().first())
        }

    private fun DbProduct.toDbDiscountedProduct(discount: DbDiscount?): DbDiscountedProduct =
        DbDiscountedProduct(code, name, price, discount?.type, discount?.params)

    private fun testProduct(
        code: String = PRODUCT_CODE,
        name: String = PRODUCT_NAME,
        price: Double = PRODUCT_PRICE,
    ): DbProduct = DbProduct(code, name, price)

    private fun testDiscount(
        type: String = DISCOUNT_TYPE,
        code: String = PRODUCT_CODE,
        params: List<Double> = listOf(0.0),
    ): DbDiscount = DbDiscount(type, code, params)
}