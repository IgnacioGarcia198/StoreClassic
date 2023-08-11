package com.garcia.ignacio.storeclassic.domain.models

import junit.framework.TestCase.assertEquals
import org.junit.Test

private const val PRODUCT_CODE = "code"
private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0
private const val ZERO_PRICE = 0.0

class DiscountTest {
    @Test
    fun `XForY discount is applied to products`() {
        val discount = Discount.XForY(PRODUCT_CODE, 2, 1)
        val products = (1..4).map { testProduct() }


        assertEquals(20.0, discount.apply(products))
    }

    @Test
    fun `XForY amount for empty list is 0`() {
        val discount = Discount.XForY(PRODUCT_CODE, 2, 1)
        val products = emptyList<Product>()


        assertEquals(ZERO_PRICE, discount.apply(products))
    }

    @Test
    fun `XForY amount for zero price products is 0`() {
        val discount = Discount.XForY(PRODUCT_CODE, 2, 1)
        val products = (1..4).map { testProduct(price = ZERO_PRICE) }


        assertEquals(ZERO_PRICE, discount.apply(products))
    }

    @Test
    fun `XForY discount is not applied if productsBought is 0`() {
        val discount = Discount.XForY(PRODUCT_CODE, 0, 1)
        val products = (1..4).map { testProduct() }


        assertEquals(PRODUCT_PRICE * 4, discount.apply(products))
    }

    @Test
    fun `XForY partitionApplicableProducts() partitions items with different product code`() {
        val discount = Discount.XForY(PRODUCT_CODE, 2, 1)
        val products = (1..4).map { testProduct() } + testProduct("")


        val (applicable, nonApplicable) = discount.partitionApplicableProducts(products)


        assertEquals((1..4).map { testProduct() }, applicable)
        assertEquals(listOf(testProduct("")), nonApplicable)
    }

    @Test
    fun `XForY partitionApplicableProducts() partitions items that do not complete a discounted group`() {
        val discount = Discount.XForY(PRODUCT_CODE, 3, 2)
        val products = (1..5).map { testProduct() }


        val (applicable, nonApplicable) = discount.partitionApplicableProducts(products)


        assertEquals((1..3).map { testProduct() }, applicable)
        assertEquals((1..2).map { testProduct() }, nonApplicable)
    }

    @Test
    fun `BuyInBulk discount is applied to products`() {
        val discount = Discount.BuyInBulk(PRODUCT_CODE, 2, 50.0)
        val products = (1..4).map { testProduct() }


        assertEquals(20.0, discount.apply(products))
    }

    @Test
    fun `BuyInBulk amount for empty list is 0`() {
        val discount = Discount.BuyInBulk(PRODUCT_CODE, 2, 50.0)
        val products = emptyList<Product>()


        assertEquals(ZERO_PRICE, discount.apply(products))
    }

    @Test
    fun `BuyInBulk amount for zero price products is 0`() {
        val discount = Discount.BuyInBulk(PRODUCT_CODE, 2, 50.0)
        val products = (1..4).map { testProduct(price = ZERO_PRICE) }


        assertEquals(ZERO_PRICE, discount.apply(products))
    }

    @Test
    fun `BuyInBulk partitionApplicableProducts() partitions items with different product code`() {
        val discount = Discount.BuyInBulk(PRODUCT_CODE, 2, 50.0)
        val products = (1..4).map { testProduct() } + testProduct("")


        val (applicable, nonApplicable) = discount.partitionApplicableProducts(products)


        assertEquals((1..4).map { testProduct() }, applicable)
        assertEquals(listOf(testProduct("")), nonApplicable)
    }

    @Test
    fun `BuyInBulk partitionApplicableProducts() partitions items that do not complete a discounted group`() {
        val discount = Discount.BuyInBulk(PRODUCT_CODE, 5, 50.0)
        val products = (1..4).map { testProduct() }


        val (applicable, nonApplicable) = discount.partitionApplicableProducts(products)


        assertEquals(emptyList<Product>(), applicable)
        assertEquals((1..4).map { testProduct() }, nonApplicable)
    }

    private fun testProduct(
        code: String = PRODUCT_CODE,
        name: String = PRODUCT_NAME,
        price: Double = PRODUCT_PRICE,
    ): Product = Product(code, name, price)
}