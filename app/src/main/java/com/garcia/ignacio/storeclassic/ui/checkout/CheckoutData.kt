package com.garcia.ignacio.storeclassic.ui.checkout

data class CheckoutData(
    val checkoutRows: List<CheckoutRow> = emptyList(),
    val totalQuantity: Int = 0,
    val totalAmount: Double = 0.0,
    val totalDiscount: Double = 0.0,
)
