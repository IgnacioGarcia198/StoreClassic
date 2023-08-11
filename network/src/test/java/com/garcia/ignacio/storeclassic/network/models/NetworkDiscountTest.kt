package com.garcia.ignacio.storeclassic.network.models

import com.garcia.ignacio.storeclassic.common.buildconfig.BuildConfig
import com.garcia.ignacio.storeclassic.data.exceptions.StoreException
import com.garcia.ignacio.storeclassic.domain.models.Discount
import junit.framework.TestCase
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

private const val PRODUCT_CODE = "code"

class NetworkDiscountTest {

    @Before
    fun setUp() {
        BuildConfig.setDebugOnStartup(false)
    }

    @Test
    fun `NetworkDiscount is normally converted to XForY`() {
        val networkDiscount = NetworkDiscount(
            Discount.XForY.TYPE,
            PRODUCT_CODE,
            listOf(2.0, 1.0)
        )


        val discount = networkDiscount.toDomain()


        TestCase.assertEquals(
            Discount.XForY(PRODUCT_CODE, 2, 1),
            discount
        )
    }

    @Test
    fun `NetworkDiscount is converted to XForY with zero as default params if no available`() {
        val networkDiscount = NetworkDiscount(
            Discount.XForY.TYPE,
            PRODUCT_CODE,
            emptyList()
        )


        val discount = networkDiscount.toDomain()


        TestCase.assertEquals(
            Discount.XForY(PRODUCT_CODE, 0, 0),
            discount
        )
    }

    @Test
    fun `NetworkDiscount conversion for XForY throws Misusing on debug if params are not available`() {
        BuildConfig.setDebugOnStartup()
        val networkDiscount = NetworkDiscount(
            Discount.XForY.TYPE,
            PRODUCT_CODE,
            emptyList()
        )


        assertThrows(StoreException.Misusing::class.java) {
            networkDiscount.toDomain()
        }
    }

    @Test
    fun `NetworkDiscount conversion for XForY throws Misusing on debug if productsBought is smaller than  productsPaid`() {
        BuildConfig.setDebugOnStartup()
        val networkDiscount = NetworkDiscount(
            Discount.XForY.TYPE,
            PRODUCT_CODE,
            listOf(1.0, 2.0)
        )


        assertThrows(StoreException.Misusing::class.java) {
            networkDiscount.toDomain()
        }
    }

    @Test
    fun `NetworkDiscount is normally converted to BuyInBulk`() {
        val networkDiscount = NetworkDiscount(
            Discount.BuyInBulk.TYPE,
            PRODUCT_CODE,
            listOf(2.0, 1.0)
        )


        val discount = networkDiscount.toDomain()


        TestCase.assertEquals(
            Discount.BuyInBulk(PRODUCT_CODE, 2, 1.0),
            discount
        )
    }

    @Test
    fun `NetworkDiscount is converted to BuyInBulk with zero as default params if no available`() {
        val networkDiscount = NetworkDiscount(
            Discount.BuyInBulk.TYPE,
            PRODUCT_CODE,
            emptyList()
        )


        val discount = networkDiscount.toDomain()


        TestCase.assertEquals(
            Discount.BuyInBulk(PRODUCT_CODE, 0, 0.0),
            discount
        )
    }

    @Test
    fun `NetworkDiscount conversion for BuyInBulk throws Misusing on debug if params are not available`() {
        BuildConfig.setDebugOnStartup()
        val networkDiscount = NetworkDiscount(
            Discount.BuyInBulk.TYPE,
            PRODUCT_CODE,
            emptyList()
        )


        assertThrows(StoreException.Misusing::class.java) {
            networkDiscount.toDomain()
        }
    }

    @Test
    fun `DbDiscount is converted to Unimplemented if type is unknown`() {
        val networkDiscount = NetworkDiscount("", PRODUCT_CODE, listOf(2.0, 1.0))


        val discount = networkDiscount.toDomain()


        TestCase.assertEquals(
            Discount.Unimplemented("", PRODUCT_CODE, listOf(2.0, 1.0)),
            discount
        )
    }
}