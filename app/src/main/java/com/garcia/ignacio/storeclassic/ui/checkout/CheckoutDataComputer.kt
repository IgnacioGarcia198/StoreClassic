package com.garcia.ignacio.storeclassic.ui.checkout

import com.garcia.ignacio.storeclassic.domain.models.DiscountedProduct
import com.garcia.ignacio.storeclassic.domain.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckoutDataComputer @Inject constructor() {

    suspend fun computeCheckoutData(
        cart: List<Product>,
        discountedProducts: List<DiscountedProduct>,
    ): List<CheckoutRow> = withContext(Dispatchers.Default) {
        val discountedRows = mutableListOf<CheckoutRow>()
        val nonDiscountedRows = mutableListOf<CheckoutRow>()
        cart.groupBy { product -> product.code }.forEach { (productCode, productGroup) ->
            val discount = discountedProducts.map {
                it.discount
            }.find {
                it?.productCode == productCode
            } ?: let {
                nonDiscountedRows.add(
                    NonDiscountedCheckoutRow(
                        productGroup,
                        productGroup.sumOf { it.price }
                    )
                )
                return@forEach
            }
            val (applicable, nonApplicable) = discount.partitionApplicableProducts(productGroup)
            if (applicable.isNotEmpty()) {
                val discountedAmount = discount.apply(applicable)
                val discountedPercent = (1 - discountedAmount / applicable.sumOf { it.price }) * 100
                discountedRows.add(
                    DiscountedCheckoutRow(applicable, discount, discountedAmount, discountedPercent)
                )
            }
            if (nonApplicable.isNotEmpty()) {
                nonDiscountedRows.add(
                    NonDiscountedCheckoutRow(nonApplicable, nonApplicable.sumOf { it.price })
                )
            }
        }
        val checkoutRows = discountedRows + nonDiscountedRows
        return@withContext if (checkoutRows.isEmpty()) checkoutRows
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
}