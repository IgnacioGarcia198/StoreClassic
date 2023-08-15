package com.garcia.ignacio.storeclassic.ui.checkout

import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.domain.models.ProductCode
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0
private const val PRODUCTS_BOUGHT = 2
private const val PRODUCTS_PAID = 1
private const val MINIMUM_BOUGHT = 3
private const val DISCOUNT_PERCENT = 5.0

class CheckoutDataComputerTest {
    private val voucher = Product(ProductCode.VOUCHER.name, PRODUCT_NAME, PRODUCT_PRICE)
    private val tShirt = Product(ProductCode.TSHIRT.name, PRODUCT_NAME, PRODUCT_PRICE)
    private val mug = Product(ProductCode.MUG.name, PRODUCT_NAME, PRODUCT_PRICE)
    private val voucherDiscount =
        Discount.XForY(ProductCode.VOUCHER.name, PRODUCTS_BOUGHT, PRODUCTS_PAID)
    private val tShirtDiscount =
        Discount.BuyInBulk(ProductCode.TSHIRT.name, MINIMUM_BOUGHT, DISCOUNT_PERCENT)
    private val discountedVoucher = DiscountedProduct(voucher, voucherDiscount)
    private val discountedTShirt = DiscountedProduct(tShirt, tShirtDiscount)

    private val computer = CheckoutDataComputer()

    @Test
    fun `computer returns NonDiscountedCheckoutRow for products with no discount`() = runBlocking {
        val cart = listOf(mug)


        val result = computer.computeCheckoutData(cart, emptyList())


        assertEquals(2, result.size)
        val mugRow = result.first() as NonDiscountedCheckoutRow
        assertEquals(PRODUCT_PRICE, mugRow.amount)
    }

    @Test
    fun `computer returns DiscountedCheckoutRow for products with discount`() = runBlocking {
        val cart = listOf(tShirt, tShirt, tShirt)


        val result = computer.computeCheckoutData(cart, listOf(discountedTShirt))


        assertEquals(2, result.size)
        val tShirtRow = result.first() as DiscountedCheckoutRow
        assertEquals(PRODUCT_PRICE * 3 * 0.95, tShirtRow.amount)
        assertEquals(5.0, tShirtRow.discountedPercent)
    }

    @Test
    fun `computer returns NonDiscountedCheckoutRow for discounted products that do not meet the conditions for discount`() =
        runBlocking {
            val cart = listOf(tShirt)


            val result = computer.computeCheckoutData(cart, listOf(discountedTShirt))


            assertEquals(2, result.size)
            val tShirtRow = result.first() as NonDiscountedCheckoutRow
            assertEquals(PRODUCT_PRICE, tShirtRow.amount)
        }

    @Test
    fun `computer groups checkout rows for same product in discounted and not discounted`() =
        runBlocking {
            val cart = listOf(voucher, voucher, voucher)


            val result = computer.computeCheckoutData(cart, listOf(discountedVoucher))


            assertEquals(3, result.size)
            val discountedRow = result.first() as DiscountedCheckoutRow
            assertEquals(PRODUCT_PRICE * 2 * 0.5, discountedRow.amount)
            assertEquals(50.0, discountedRow.discountedPercent)
            val nonDiscountedRow = result[1] as NonDiscountedCheckoutRow
            assertEquals(PRODUCT_PRICE, nonDiscountedRow.amount)
        }

    @Test
    fun `computer adds at the end one checkout row for totals`() = runBlocking {
        val cart = listOf(voucher, voucher, voucher, mug)


        val result = computer.computeCheckoutData(cart, listOf(discountedVoucher))


        assertEquals(4, result.size)
        val discountedVoucherRow = result.first() as DiscountedCheckoutRow
        assertEquals(PRODUCT_PRICE * 2 * 0.5, discountedVoucherRow.amount)
        val nonDiscountedVoucherRow = result[1] as NonDiscountedCheckoutRow
        assertEquals(PRODUCT_PRICE, nonDiscountedVoucherRow.amount)
        val mugRow = result[2] as NonDiscountedCheckoutRow
        assertEquals(PRODUCT_PRICE, mugRow.amount)
        val totalRow = result[3] as TotalCheckoutRow
        assertEquals(PRODUCT_PRICE * 2 + PRODUCT_PRICE * 2 * 0.5, totalRow.amount)
        assertEquals((1 - totalRow.amount / (PRODUCT_PRICE * 4)) * 100, totalRow.discountedPercent)
    }
}