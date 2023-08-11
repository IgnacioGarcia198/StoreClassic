package com.garcia.ignacio.storeclassic.db.storage

import com.garcia.ignacio.storeclassic.db.dao.DiscountDao
import com.garcia.ignacio.storeclassic.db.models.toDbDiscount
import com.garcia.ignacio.storeclassic.domain.models.Discount
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

private const val PRODUCT_CODE = "code"

class DiscountsStorageTest {
    private val discountDao: DiscountDao = mockk(relaxed = true)
    private val storage = DiscountsStorage(discountDao)

    @Test
    fun `storage uses dao to insert discounts`() = runBlocking {
        storage.updateDiscounts(listOf(testDiscount))


        coVerify { discountDao.insertAll(listOf(testDiscount.toDbDiscount())) }
    }

    @Test
    fun `storage uses dao to retrieve discounts`() = runBlocking {
        every { discountDao.getAll() }.returns(flowOf(listOf(testDiscount.toDbDiscount())))


        val result = storage.getAllDiscounts()


        coVerify { discountDao.getAll() }
        TestCase.assertEquals(listOf(testDiscount), result.first())
    }

    private val testDiscount = Discount.XForY(PRODUCT_CODE, 3, 2)
}