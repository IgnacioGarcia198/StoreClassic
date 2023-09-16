package com.garcia.ignacio.storeclassic.domain.checkout

import com.garcia.ignacio.storeclassic.domain.models.CheckoutRow
import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.DiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.NonDiscountedCheckoutRow
import com.garcia.ignacio.storeclassic.domain.models.Product
import com.garcia.ignacio.storeclassic.domain.models.TotalCheckoutRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StoreCheckoutDataComputer @Inject constructor() : CheckoutDataComputer {

    override suspend fun computeCheckoutData(
        cart: List<Product>,
        discountedProducts: List<DiscountedProduct>,
    ): List<CheckoutRow> = withContext(Dispatchers.Default) {
        val discountedRows = mutableListOf<CheckoutRow>()
        val nonDiscountedRows = mutableListOf<CheckoutRow>()
        cart.groupBy { product -> product.code }.forEach { (productCode, productGroup) ->
            val discount = findDiscountForProduct(
                discountedProducts,
                productCode,
            ) ?: let {
                nonDiscountedRows.add(
                    NonDiscountedCheckoutRow(
                        productGroup,
                        productGroup.sumOf { it.price }
                    )
                )
                return@forEach
            }
            partitionDiscountedProductGroup(
                discount,
                productGroup,
                discountedRows,
                nonDiscountedRows
            )
        }
        val checkoutRows = discountedRows + nonDiscountedRows
        return@withContext addTotalRowIfNeeded(checkoutRows, cart)
    }

    private fun addTotalRowIfNeeded(
        checkoutRows: List<CheckoutRow>,
        cart: List<Product>
    ): List<CheckoutRow> {
        return if (checkoutRows.isEmpty()) checkoutRows
        else {
            val totalAmount = checkoutRows.sumOf { it.amount }
            val originalAmount = cart.sumOf { it.price }
            val totalRow = TotalCheckoutRow(
                quantity = cart.size,
                amount = totalAmount,
                discountedPercent = (1 - totalAmount / originalAmount) * 100
            )
            checkoutRows + totalRow
        }
    }

    private fun partitionDiscountedProductGroup(
        discount: Discount,
        productGroup: List<Product>,
        discountedRows: MutableList<CheckoutRow>,
        nonDiscountedRows: MutableList<CheckoutRow>
    ) {
        val (applicable, nonApplicable) = discount.partitionApplicableProducts(productGroup)
        if (applicable.isNotEmpty()) {
            val discountedAmount = discount.apply(applicable)
            val discountedPercent = (1 - discountedAmount / applicable.sumOf { it.price }) * 100
            discountedRows.add(
                DiscountedCheckoutRow(applicable, discountedAmount, discountedPercent)
            )
        }
        if (nonApplicable.isNotEmpty()) {
            nonDiscountedRows.add(
                NonDiscountedCheckoutRow(nonApplicable, nonApplicable.sumOf { it.price })
            )
        }
    }

    private fun findDiscountForProduct(
        discountedProducts: List<DiscountedProduct>,
        productCode: String,
    ): Discount? = discountedProducts.map {
        it.discount
    }.find {
        it?.productCode == productCode
    }
}