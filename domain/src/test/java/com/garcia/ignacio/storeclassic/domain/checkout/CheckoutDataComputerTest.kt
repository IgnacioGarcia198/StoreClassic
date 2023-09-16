package com.garcia.ignacio.storeclassic.domain.checkout

import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.NonDiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.domain.models.ProductCode
import com.garcia.ignacio.storeclassic.domain.models.TotalCheckoutRow
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test

private const val PRODUCT_NAME = "name"
private const val PRODUCT_PRICE = 10.0
private const val PRODUCTS_BOUGHT = 2
private const val PRODUCTS_PAID = 1
private const val MINIMUM_BOUGHT = 3
private const val DISCOUNT_PERCENT = 5.0
private const val DELTA = 0.0001
class CheckoutDataComputerTest {
    private val voucher = Product(
        ProductCode.VOUCHER.name,
        PRODUCT_NAME,
        PRODUCT_PRICE
    )
    private val tShirt = Product(
        ProductCode.TSHIRT.name,
        PRODUCT_NAME,
        PRODUCT_PRICE
    )
    private val mug = Product(
        ProductCode.MUG.name,
        PRODUCT_NAME,
        PRODUCT_PRICE
    )
    private val voucherDiscount =
        Discount.XForY(
            ProductCode.VOUCHER.name,
            PRODUCTS_BOUGHT,
            PRODUCTS_PAID
        )
    private val tShirtDiscount =
        Discount.BuyInBulk(
            ProductCode.TSHIRT.name,
            MINIMUM_BOUGHT,
            DISCOUNT_PERCENT
        )
    private val discountedVoucher = DiscountedProduct(voucher, voucherDiscount)
    private val discountedTShirt = DiscountedProduct(tShirt, tShirtDiscount)

    private val computer = StoreCheckoutDataComputer()

    @Test
    fun `computer returns NonDiscountedCheckoutRow for products with no discount`() = runBlocking {
        val cart = listOf(mug)


        val result = computer.computeCheckoutData(cart, emptyList())


        TestCase.assertEquals(2, result.size)
        val mugRow =
            result.first() as NonDiscountedCheckoutRow
        TestCase.assertEquals(
            PRODUCT_PRICE,
            mugRow.amount
        )
    }

    @Test
    fun `computer returns DiscountedCheckoutRow for products with discount`() = runBlocking {
        val cart = listOf(tShirt, tShirt, tShirt)


        val result = computer.computeCheckoutData(cart, listOf(discountedTShirt))


        TestCase.assertEquals(2, result.size)
        val tShirtRow =
            result.first() as DiscountedCheckoutRow
        TestCase.assertEquals(
            PRODUCT_PRICE * 3 * 0.95,
            tShirtRow.amount,
            DELTA
        )
        TestCase.assertEquals(
            5.0,
            tShirtRow.discountedPercent,
            DELTA
        )
    }

    @Test
    fun `computer returns NonDiscountedCheckoutRow for discounted products that do not meet the conditions for discount`() =
        runBlocking {
            val cart = listOf(tShirt)


            val result = computer.computeCheckoutData(cart, listOf(discountedTShirt))


            TestCase.assertEquals(2, result.size)
            val tShirtRow =
                result.first() as NonDiscountedCheckoutRow
            TestCase.assertEquals(
                PRODUCT_PRICE,
                tShirtRow.amount
            )
        }

    @Test
    fun `computer groups checkout rows for same product in discounted and not discounted`() =
        runBlocking {
            val cart = listOf(voucher, voucher, voucher)


            val result = computer.computeCheckoutData(cart, listOf(discountedVoucher))


            TestCase.assertEquals(3, result.size)
            val discountedRow =
                result.first() as DiscountedCheckoutRow
            TestCase.assertEquals(
                PRODUCT_PRICE * 2 * 0.5,
                discountedRow.amount,
                DELTA
            )
            TestCase.assertEquals(
                50.0,
                discountedRow.discountedPercent,
                DELTA
            )
            val nonDiscountedRow =
                result[1] as NonDiscountedCheckoutRow
            TestCase.assertEquals(
                PRODUCT_PRICE,
                nonDiscountedRow.amount,
                DELTA
            )
        }

    @Test
    fun `computer adds at the end one checkout row for totals`() = runBlocking {
        val cart = listOf(voucher, voucher, voucher, mug)


        val result = computer.computeCheckoutData(cart, listOf(discountedVoucher))


        TestCase.assertEquals(4, result.size)
        val discountedVoucherRow =
            result.first() as DiscountedCheckoutRow
        TestCase.assertEquals(
            PRODUCT_PRICE * 2 * 0.5,
            discountedVoucherRow.amount,
            DELTA
        )
        val nonDiscountedVoucherRow =
            result[1] as NonDiscountedCheckoutRow
        TestCase.assertEquals(
            PRODUCT_PRICE,
            nonDiscountedVoucherRow.amount
        )
        val mugRow =
            result[2] as NonDiscountedCheckoutRow
        TestCase.assertEquals(
            PRODUCT_PRICE,
            mugRow.amount
        )
        val totalRow = result[3] as TotalCheckoutRow
        TestCase.assertEquals(
            PRODUCT_PRICE * 2 + PRODUCT_PRICE * 2 * 0.5,
            totalRow.amount,
            DELTA
        )
        TestCase.assertEquals(
            (1 - totalRow.amount / (PRODUCT_PRICE * 4)) * 100,
            totalRow.discountedPercent,
            DELTA
        )
    }
}