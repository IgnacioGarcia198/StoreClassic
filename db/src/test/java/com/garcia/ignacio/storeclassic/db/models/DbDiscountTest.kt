package com.garcia.ignacio.storeclassic.db.models

import com.garcia.ignacio.storeclassic.domain.models.Discount
import junit.framework.TestCase.assertEquals
import org.junit.Test

private const val PRODUCT_CODE = "code"

class DbDiscountTest {

    @Test
    fun `DbDiscount is normally converted to XForY`() {
        val dbDiscount = DbDiscount(Discount.XForY.TYPE, PRODUCT_CODE, listOf(2.0, 1.0))


        val discount = dbDiscount.toDomain()


        assertEquals(
            Discount.XForY(PRODUCT_CODE, 2, 1),
            discount
        )
    }

    @Test
    fun `DbDiscount is converted to XForY with zero as default params if no available`() {
        val dbDiscount = DbDiscount(Discount.XForY.TYPE, PRODUCT_CODE, emptyList())


        val discount = dbDiscount.toDomain()


        assertEquals(
            Discount.XForY(PRODUCT_CODE, 0, 0),
            discount
        )
    }

    @Test
    fun `DbDiscount is normally converted to BuyInBulk`() {
        val dbDiscount = DbDiscount(Discount.BuyInBulk.TYPE, PRODUCT_CODE, listOf(2.0, 1.0))


        val discount = dbDiscount.toDomain()


        assertEquals(
            Discount.BuyInBulk(PRODUCT_CODE, 2, 1.0),
            discount
        )
    }

    @Test
    fun `DbDiscount is converted to BuyInBulk with zero as default params if no available`() {
        val dbDiscount = DbDiscount(Discount.BuyInBulk.TYPE, PRODUCT_CODE, emptyList())


        val discount = dbDiscount.toDomain()


        assertEquals(
            Discount.BuyInBulk(PRODUCT_CODE, 0, 0.0),
            discount
        )
    }

    @Test
    fun `DbDiscount is converted to Unimplemented if type is unknown`() {
        val dbDiscount = DbDiscount("", PRODUCT_CODE, listOf(2.0, 1.0))


        val discount = dbDiscount.toDomain()


        assertEquals(
            Discount.Unimplemented("", PRODUCT_CODE, listOf(2.0, 1.0)),
            discount
        )
    }
}