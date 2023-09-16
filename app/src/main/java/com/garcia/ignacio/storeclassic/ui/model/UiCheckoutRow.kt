package com.garcia.ignacio.storeclassic.ui.model

import com.garcia.ignacio.storeclassic.domain.models.CheckoutRow
import com.garcia.ignacio.storeclassic.domain.models.TotalCheckoutRow

data class UiCheckoutRow(
    val quantity: Int,
    val amount: Double,
    val discountedPercent: Double,
    val isTotalRow: Boolean,
    val name: String = "",
    val price: Double = 0.0,
)

fun CheckoutRow.toUi(): UiCheckoutRow = when (this) {
    is TotalCheckoutRow -> UiCheckoutRow(quantity, amount, discountedPercent, true)
    else -> UiCheckoutRow(
        products.size,
        amount,
        discountedPercent,
        false,
        products.first().name,
        products.first().price
    )
}
