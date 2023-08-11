package com.garcia.ignacio.storeclassic.db.dao

import com.garcia.ignacio.storeclassic.db.database.DatabaseTest
import com.garcia.ignacio.storeclassic.db.models.DbDiscount
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val DISCOUNT_TYPE = "type"

class DiscountDaoTest : DatabaseTest() {
    private lateinit var discountDao: DiscountDao

    @Before
    fun setUp() {
        discountDao = db.discountDao
    }

    @Test
    fun `insertAll() inserts a list of discounts that can be retrieved`() = runBlocking {
        val discounts = (1..10).map { testDiscount(code = "$PRODUCT_CODE$it") }
        discountDao.insertAll(discounts)


        assertEquals(discounts, discountDao.getAll().first())
    }

    @Test
    fun `insertAll() replaces discounts with same product code and discount type`() = runBlocking {
        val discounts = (1..10).map { testDiscount() }
        discountDao.insertAll(discounts)


        assertEquals(listOf(testDiscount()), discountDao.getAll().first())
    }

    private fun testDiscount(
        type: String = DISCOUNT_TYPE,
        code: String = PRODUCT_CODE,
        params: List<Double> = listOf(0.0),
    ): DbDiscount = DbDiscount(type, code, params)
}