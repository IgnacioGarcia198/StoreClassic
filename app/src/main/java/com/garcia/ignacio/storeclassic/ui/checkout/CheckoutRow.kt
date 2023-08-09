package com.garcia.ignacio.storeclassic.ui.checkout

import com.garcia.ignacio.storeclassic.domain.models.Discount
import com.garcia.ignacio.storeclassic.domain.models.Product

sealed interface CheckoutRow {
    val products: List<Product>
    val amount: Double
    val discountedPercent: Double
}

data class DiscountedCheckoutRow(
    override val products: List<Product>,
    val discount: Discount,
    override val amount: Double,
    override val discountedPercent: Double,
) : CheckoutRow

data class NonDiscountedCheckoutRow(
    override val products: List<Product>,
    override val amount: Double,
) : CheckoutRow {
    override val discountedPercent: Double = 0.0
}

data class TotalCheckoutRow(
    val quantity: Int,
    override val amount: Double,
    override val discountedPercent: Double,
) : CheckoutRow {
    override val products: List<Product> = emptyList()
}
